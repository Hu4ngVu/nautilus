package vn.io.huangnosimp.nautilusserver.server.services;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateServer;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.server.ServerCreatedEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.ChangeServerVisibility;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.star.StarUtils;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
    private final AuthUserUtils authUserUtils;
    private final ServerRepository serverRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GalleryService galleryService;
    private final StorageUtils storageUtils;
    private final StarUtils starUtils;

    @Override
    public Page<ServerInfoResponse> getAllServer(
            Boolean requiresPremium,
            ServerType serverType,
            ServerPlatform serverPlatform,
            List<String> tags,
            Pageable pageable) {
        log.debug(
                "Fetching all servers with filters: requiresPremium={}, serverType={},"
                        + " serverPlatform={}, tags={}",
                requiresPremium,
                serverType,
                serverPlatform,
                tags);
        Specification<Server> specification =
                getServerSpecification(requiresPremium, serverType, serverPlatform, tags);
        var response =
                serverRepository
                        .findAllByVisibilityIsTrue(specification, pageable)
                        .map(
                                server ->
                                        ServerInfoResponse.builder()
                                                .avatarUrl(
                                                        storageUtils.createSignedGetUrl(
                                                                server.getId(),
                                                                StorageType.SERVER_AVATAR))
                                                .requiresPremium(server.getRequiresPremium())
                                                .servername(server.getServername())
                                                .summary(server.getSummary())
                                                .starCount(server.getStarCount())
                                                .build());
        log.info(
                "All servers fetched successfully with filters: requiresPremium={}, serverType={},"
                        + " serverPlatform={}, tags={}",
                requiresPremium,
                serverType,
                serverPlatform,
                tags);
        return response;
    }

    @Override
    public ServerInfoDetailResponse getServerInfo(String username, String servername)
            throws ServerNotFound {
        log.debug("Fetching server info for user: {}, server: {}", username, servername);
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        Server server =
                serverRepository
                        .findByUserIdAndServername(userId, servername)
                        .orElseThrow(() -> new ServerNotFound(servername));
        var response =
                ServerInfoDetailResponse.builder()
                        .servername(server.getServername())
                        .visibility(server.getVisibility())
                        .requiresPremium(server.getRequiresPremium())
                        .javaAddress(server.getJavaAddress())
                        .bedrockAddress(server.getBedrockAddress())
                        .avatarUrl(
                                storageUtils.createSignedGetUrl(
                                        server.getId(), StorageType.SERVER_AVATAR))
                        .bannerUrl(
                                storageUtils.createSignedGetUrl(
                                        server.getId(), StorageType.SERVER_BANNER))
                        .summary(server.getSummary())
                        .bio(server.getBio())
                        .starCount(server.getStarCount())
                        .build();
        log.info("Server info for user: {}, server: {} fetched successfully", username, servername);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createServer(String username, CreateServerRequest body) {
        log.debug("Creating server for user: {}, servername: {}", username, body.servername());
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        if (serverRepository.existsByUserIdAndServername(userId, body.servername())) {
            throw new DuplicateServer(
                    "Server with name "
                            + body.servername()
                            + " already exists for user "
                            + username);
        }
        Server server =
                Server.builder()
                        .userId(userId)
                        .servername(body.servername())
                        .requiresPremium(body.requiresPremium())
                        .visibility(body.visibility())
                        .type(body.type())
                        .summary(body.summary())
                        .platform(body.platform())
                        .starCount(0)
                        .build();
        serverRepository.save(server);
        eventPublisher.publishEvent(new ServerCreatedEvent(userId));
        log.info(
                "Server for user: {}, servername: {} created successfully",
                username,
                body.servername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServer(String username, String servername) {
        log.debug("Deleting server for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);
        galleryService.deleteGallery(server.getUserId(), servername);
        storageUtils.setResourceDeleted(
                server.getId(), StorageType.SERVER_BANNER, server.getUserId());
        storageUtils.setResourceDeleted(
                server.getId(), StorageType.SERVER_AVATAR, server.getUserId());
        serverRepository.delete(server);
        eventPublisher.publishEvent(new ServerDeletedEvent(server.getUserId(), server.getId()));
        log.info("Server for user: {}, servername: {} deleted successfully", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServerInfo(String username, String servername, UpdateServerInfoRequest body) {
        log.debug("Updating server info for user: {}, server: {}", username, servername);
        if (allFieldIsNull(body)) {
            return;
        }
        Server server = getServer(username, servername);

        if (body.avatarResourceId() != null) {
            storageUtils.setResourceDeleted(
                    server.getId(), StorageType.SERVER_AVATAR, server.getUserId());
            storageUtils.setResourceActive(
                    body.avatarResourceId(),
                    server.getId(),
                    StorageType.SERVER_AVATAR,
                    server.getUserId());
        }
        if (body.bannerResourceId() != null) {
            storageUtils.setResourceDeleted(
                    server.getId(), StorageType.SERVER_BANNER, server.getUserId());
            storageUtils.setResourceActive(
                    body.bannerResourceId(),
                    server.getId(),
                    StorageType.SERVER_BANNER,
                    server.getUserId());
        }
        if (body.javaAddress() != null) {
            server.setJavaAddress(body.javaAddress());
        }
        if (body.bedrockAddress() != null) {
            server.setBedrockAddress(body.bedrockAddress());
        }
        if (body.summary() != null) {
            server.setSummary(body.summary());
        }
        if (body.bio() != null) {
            server.setBio(body.bio());
        }
        if (body.requiresPremium() != null
                && !body.requiresPremium().equals(server.getRequiresPremium())) {
            server.setRequiresPremium(body.requiresPremium());
        }
        serverRepository.save(server);
        log.info("Server info for user: {}, server: {} updated successfully", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServerAvatar(String username, String servername) {
        log.debug("Deleting server image for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);
        storageUtils.setResourceDeleted(
                server.getId(), StorageType.SERVER_AVATAR, server.getUserId());
        log.info(
                "Server image for user: {}, server: {} deleted successfully", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServerBanner(String username, String servername) {
        log.debug("Deleting server banner for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);
        storageUtils.setResourceDeleted(
                server.getId(), StorageType.SERVER_BANNER, server.getUserId());
        log.info(
                "Server banner for user: {}, server: {} deleted successfully",
                username,
                servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeServername(String username, String servername, String newServername) {
        log.debug(
                "Changing server name for user: {}, server: {} to {}",
                username,
                servername,
                newServername);
        Server server = getServer(username, servername);
        if (serverRepository.existsByUserIdAndServername(server.getUserId(), newServername)) {
            throw new DuplicateServer(
                    "Server with name " + newServername + " already exists for user " + username);
        }
        server.setServername(newServername);
        serverRepository.save(server);
        log.info(
                "Server name for user: {}, server: {} changed to {}",
                username,
                servername,
                newServername);
    }

    @Override
    public void changeServerVisibility(
            String username, String servername, ChangeServerVisibility request) {
        log.debug(
                "Changing server visibility for user: {}, server: {} to {}",
                username,
                servername,
                request.visibility());
        Server server = getServer(username, servername);
        if (server.getVisibility().equals(request.visibility())) {
            return;
        }
        server.setVisibility(request.visibility());
        serverRepository.save(server);
        if (request.visibility().equals(Boolean.FALSE)) {
            starUtils.deleteAllStar(server.getId());
        }
        log.info(
                "Server visibility for user: {}, server: {} changed to {}",
                username,
                servername,
                request.visibility());
    }

    private Specification<Server> getServerSpecification(
            Boolean requiresPremium,
            ServerType serverType,
            ServerPlatform serverPlatform,
            List<String> tags) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            if (requiresPremium != null) {
                predicates
                        .getExpressions()
                        .add(criteriaBuilder.equal(root.get("requiresPremium"), requiresPremium));
            }
            if (serverType != null) {
                predicates
                        .getExpressions()
                        .add(criteriaBuilder.equal(root.get("type"), serverType));
            }
            if (serverPlatform != null) {
                predicates
                        .getExpressions()
                        .add(criteriaBuilder.equal(root.get("platform"), serverPlatform));
            }
            if (tags != null && !tags.isEmpty()) {
                Subquery<Server> subquery = query.subquery(Server.class);
                Root<ServerTag> subRoot = subquery.from(ServerTag.class);
                subquery.select(subRoot.get("server"));
                subquery.where(subRoot.get("tag").in(tags));
                predicates.getExpressions().add(criteriaBuilder.in(root).value(subquery));
            }

            return predicates;
        };
    }

    private Server getServer(String username, String servername) {
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        return serverRepository
                .findByUserIdAndServername(userId, servername)
                .orElseThrow(() -> new ServerNotFound(servername));
    }

    private boolean allFieldIsNull(UpdateServerInfoRequest body) {
        return body.avatarResourceId() == null
                && body.bannerResourceId() == null
                && body.javaAddress() == null
                && body.bedrockAddress() == null
                && body.bio() == null
                && body.summary() == null;
    }
}
