package vn.io.huangnosimp.nautilusserver.server.services;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import java.util.UUID;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.server.DeleteServerAvatarEvent;
import vn.io.huangnosimp.nautilusserver.server.DeleteServerBannerEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerCreatedEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.server.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.server.exceptions.UsernameNotFound;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.storage.StorageService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
    private final AuthUserUtils authUserUtils;
    private final ServerRepository serverRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GalleryService galleryService;
    private final StorageService storageService;

    @Override
    public Page<ServerInfoResponse> getAllServer(
            Boolean requiresPremium,
            ServerType serverType,
            ServerPlatform serverPlatform,
            List<String> tags,
            Pageable pageable) {
        Specification<Server> specification =
                getServerSpecification(requiresPremium, serverType, serverPlatform, tags);

        return serverRepository
                .findAllByVisibilityIsTrue(specification, pageable)
                .map(
                        server ->
                                ServerInfoResponse.builder()
                                        .avatarUrl(
                                                server.getAvatarKey() != null
                                                        ? storageService.createSignedGetUrl(
                                                                server.getAvatarKey())
                                                        : null)
                                        .livePlayerCount(null)
                                        .maxPlayerCount(null)
                                        .requiresPremium(server.getRequiresPremium())
                                        .servername(server.getServername())
                                        .summary(server.getSummary())
                                        .build());
    }

    @Override
    public ServerInfoDetailResponse getServerInfo(String username, String servername)
            throws ServerNotFound {
        log.debug("Fetching server info for user: {}, server: {}", username, servername);
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UsernameNotFound(username));
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
                                server.getAvatarKey() != null
                                        ? storageService.createSignedGetUrl(server.getAvatarKey())
                                        : null)
                        .bannerUrl(
                                server.getBannerKey() != null
                                        ? storageService.createSignedGetUrl(server.getBannerKey())
                                        : null)
                        .summary(server.getSummary())
                        .bio(server.getBio())
                        .livePlayerCount(null)
                        .maxPlayerCount(null)
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
                        .orElseThrow(() -> new UsernameNotFound(username));
        Server server =
                Server.builder()
                        .userId(userId)
                        .servername(body.servername())
                        .requiresPremium(body.requiresPremium())
                        .visibility(body.visibility())
                        .type(body.type())
                        .summary(body.summary())
                        .platform(body.platform())
                        .build();
        serverRepository.save(server);
        eventPublisher.publishEvent(new ServerCreatedEvent(username));
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
        if (server.getBannerKey() != null) {
            eventPublisher.publishEvent(new DeleteServerBannerEvent(server.getBannerKey()));
        }
        if (server.getAvatarKey() != null) {
            eventPublisher.publishEvent(new DeleteServerAvatarEvent(server.getAvatarKey()));
        }
        serverRepository.delete(server);
        eventPublisher.publishEvent(new ServerDeletedEvent(username));
        log.info("Server for user: {}, servername: {} deleted successfully", username, servername);
    }

    @Override
    public void updateServerInfo(String username, String servername, UpdateServerInfoRequest body) {
        log.debug("Updating server info for user: {}, server: {}", username, servername);
        if (allFieldIsNull(body)) {
            return;
        }
        Server server = getServer(username, servername);

        String oldBannerKey = server.getBannerKey();
        String oldAvatarKey = server.getAvatarKey();
        String newBannerKey = null;
        String newAvatarKey = null;
        boolean isBannerUploaded = false;
        boolean isAvatarUploaded = false;

        MultipartFile bannerFile = body.banner();
        MultipartFile avatarFile = body.image();

        if (body.banner() != null) {
            String extension = FileNameUtil.getExtension(body.banner().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException(
                        "Invalid file type. Only PNG, JPG, JPEG, WEBP, and GIF are allowed.");
            }
            newBannerKey =
                    "server/banner/"
                            + username
                            + "/"
                            + server.getServername()
                            + "-"
                            + UUID.randomUUID()
                            + "."
                            + extension;

            storageService.uploadImage(bannerFile, newBannerKey);
            isBannerUploaded = true;
        }
        if (body.image() != null) {
            String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException(
                        "Invalid file type. Only PNG, JPG, JPEG, WEBP, and GIF are allowed.");
            }
            newAvatarKey =
                    "server/avatar/"
                            + username
                            + "/"
                            + server.getServername()
                            + "-"
                            + UUID.randomUUID()
                            + "."
                            + extension;
            storageService.uploadImage(avatarFile, newAvatarKey);
            isAvatarUploaded = true;
        }

        try {
            if (body.javaAddress() != null) {
                server.setJavaAddress(body.javaAddress());
            }
            if (body.bedrockAddress() != null) {
                server.setBedrockAddress(body.bedrockAddress());
            }
            if (body.bio() != null) {
                server.setBio(body.bio());
            }
            if (isBannerUploaded) {
                server.setBannerKey(newBannerKey);
            }
            if (isAvatarUploaded) {
                server.setAvatarKey(newAvatarKey);
            }
            serverRepository.save(server);
            log.info(
                    "Server info for user: {}, server: {} updated successfully",
                    username,
                    servername);
        } catch (Exception e) {
            if (isBannerUploaded) {
                eventPublisher.publishEvent(new DeleteServerBannerEvent(newBannerKey));
            }
            if (isAvatarUploaded) {
                eventPublisher.publishEvent(new DeleteServerAvatarEvent(newAvatarKey));
            }
            throw e;
        }

        if (isBannerUploaded && oldBannerKey != null) {
            eventPublisher.publishEvent(new DeleteServerBannerEvent(oldBannerKey));
        }
        if (isAvatarUploaded && oldAvatarKey != null) {
            eventPublisher.publishEvent(new DeleteServerAvatarEvent(oldAvatarKey));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServerAvatar(String username, String servername) {
        log.debug("Deleting server image for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);
        String oldKey = server.getAvatarKey();
        server.setAvatarKey(null);
        serverRepository.save(server);
        if (oldKey != null) {
            eventPublisher.publishEvent(new DeleteServerAvatarEvent(oldKey));
        }
        log.info(
                "Server image for user: {}, server: {} deleted successfully", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServerBanner(String username, String servername) {
        log.debug("Deleting server banner for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);
        String oldKey = server.getBannerKey();
        server.setBannerKey(null);
        serverRepository.save(server);
        if (oldKey != null) {
            eventPublisher.publishEvent(new DeleteServerBannerEvent(oldKey));
        }
        log.info(
                "Server banner for user: {}, server: {} deleted successfully",
                username,
                servername);
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
                        .orElseThrow(() -> new UsernameNotFound(username));
        return serverRepository
                .findByUserIdAndServername(userId, servername)
                .orElseThrow(() -> new ServerNotFound(servername));
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || !extension.equalsIgnoreCase("png")
                        && !extension.equalsIgnoreCase("jpg")
                        && !extension.equalsIgnoreCase("jpeg")
                        && !extension.equalsIgnoreCase("webp")
                        && !extension.equalsIgnoreCase("gif");
    }

    private boolean allFieldIsNull(UpdateServerInfoRequest body) {
        return body.banner() == null
                && body.image() == null
                && body.javaAddress() == null
                && body.bedrockAddress() == null
                && body.bio() == null;
    }
}
