package vn.io.huangnosimp.nautilusserver.server.services;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.events.*;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateServer;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
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

@Service
@RequiredArgsConstructor
public class ServerService {
    private final ServerRepository serverRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GalleryService galleryService;
    private final ResourceStorage resourceStorage;

    @Logging(logArguments = true)
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
                                                resourceStorage.createSignedGetUrl(
                                                        server.getServerAvatarId()))
                                        .requiresPremium(server.getRequiresPremium())
                                        .servername(server.getServername())
                                        .summary(server.getSummary())
                                        .starCount(server.getStarCount())
                                        .build());
    }

    @Logging(logArguments = true)
    @Cacheable(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#serverId")
    public ServerInfoDetailResponse getServerInfo(String serverId) throws ServerNotFound {
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
        return ServerInfoDetailResponse.builder()
                .servername(server.getServername())
                .visibility(server.getVisibility())
                .requiresPremium(server.getRequiresPremium())
                .javaAddress(server.getJavaAddress())
                .bedrockAddress(server.getBedrockAddress())
                .avatarUrl(resourceStorage.createSignedGetUrl(server.getServerAvatarId()))
                .bannerUrl(resourceStorage.createSignedGetUrl(server.getServerBannerId()))
                .summary(server.getSummary())
                .bio(server.getBio())
                .starCount(server.getStarCount())
                .build();
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void createServer(String authUserId, CreateServerRequest body) {
        if (serverRepository.existsByUserIdAndServername(authUserId, body.servername())) {
            throw new DuplicateServer(
                    "Server with name "
                            + body.servername()
                            + " already exists for user "
                            + authUserId);
        }
        Server server =
                Server.builder()
                        .userId(authUserId)
                        .servername(body.servername())
                        .requiresPremium(body.requiresPremium())
                        .visibility(body.visibility())
                        .type(body.type())
                        .summary(body.summary())
                        .platform(body.platform())
                        .starCount(0)
                        .build();
        serverRepository.save(server);
        eventPublisher.publishEvent(
                ServerCreatedEvent.builder()
                        .userId(authUserId)
                        .serverId(server.getId())
                        .isPublic(server.getVisibility())
                        .build());
    }

    @Logging(logArguments = true)
    @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#serverId")
    @Transactional(rollbackFor = Exception.class)
    public void deleteServer(String serverId) {
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
        galleryService.deleteGallery(server.getId());
        serverRepository.delete(server);
        eventPublisher.publishEvent(
                ServerDeletedEvent.builder()
                        .userId(server.getUserId())
                        .serverId(server.getId())
                        .wasPublic(server.getVisibility())
                        .serverAvatarId(server.getServerAvatarId())
                        .serverBannerId(server.getServerBannerId())
                        .build());
    }

    @Logging(logArguments = true)
    @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#serverId")
    @Transactional(rollbackFor = Exception.class)
    public void updateServerInfo(String serverId, UpdateServerInfoRequest body) {
        if (allFieldIsNull(body)) {
            return;
        }
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));

        if (body.avatarResourceId() != null) {
            if (server.getServerAvatarId() != null) {
                eventPublisher.publishEvent(
                        new ServerAvatarDeletedEvent(server.getServerAvatarId()));
            }
            eventPublisher.publishEvent(new ServerAvatarUpdatedEvent(body.avatarResourceId()));
            server.setServerAvatarId(body.avatarResourceId());
        }
        if (body.bannerResourceId() != null) {
            if (server.getServerBannerId() != null) {
                eventPublisher.publishEvent(
                        new ServerBannerDeletedEvent(server.getServerBannerId()));
            }
            eventPublisher.publishEvent(new ServerBannerUpdatedEvent(body.bannerResourceId()));
            server.setServerBannerId(body.bannerResourceId());
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
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#serverId")
    public void deleteServerAvatar(String serverId) {
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
        if (server.getServerAvatarId() != null) {
            eventPublisher.publishEvent(new ServerAvatarDeletedEvent(server.getServerAvatarId()));
            server.setServerAvatarId(null);
            serverRepository.save(server);
        }
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#serverId")
    public void deleteServerBanner(String serverId) {
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
        if (server.getServerBannerId() != null) {
            eventPublisher.publishEvent(new ServerBannerDeletedEvent(server.getServerBannerId()));
            server.setServerBannerId(null);
            serverRepository.save(server);
        }
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#serverId")
    public void changeServername(String serverId, String newServername) {
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
        if (serverRepository.existsByUserIdAndServername(server.getUserId(), newServername)) {
            throw new DuplicateServer(
                    "Server with name "
                            + newServername
                            + " already exists for user "
                            + server.getUserId());
        }
        server.setServername(newServername);
        serverRepository.save(server);
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @Caching(
            evict = {
                @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#serverId"),
                @CacheEvict(cacheNames = CacheName.PUBLIC_SERVER_CACHE, key = "#serverId")
            })
    public void changeServerVisibility(String serverId, ChangeServerVisibility request) {
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
        if (server.getVisibility().equals(request.visibility())) {
            return;
        }
        server.setVisibility(request.visibility());
        serverRepository.save(server);
        eventPublisher.publishEvent(
                ServerVisibilityChangedEvent.builder()
                        .serverId(server.getId())
                        .isPublic(server.getVisibility())
                        .build());
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

    private boolean allFieldIsNull(UpdateServerInfoRequest body) {
        return body.avatarResourceId() == null
                && body.bannerResourceId() == null
                && body.javaAddress() == null
                && body.bedrockAddress() == null
                && body.bio() == null
                && body.summary() == null;
    }
}
