package vn.io.huangnosimp.nautilusserver.server.services;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.api.AuthUserQuery;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.events.*;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateServer;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidResourceId;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
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
import vn.io.huangnosimp.nautilusserver.server.repositories.BedrockMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.JavaMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.TagRepository;

@Service
@RequiredArgsConstructor
public class ServerService {
    private final ServerRepository serverRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GalleryService galleryService;
    private final ResourceStorage resourceStorage;
    private final AuthUserQuery authUserQuery;
    private final TagRepository tagRepository;
    private final JavaMcVersionRepository javaMcVersionRepository;
    private final BedrockMcVersionRepository bedrockMcVersionRepository;

    private static final int MAX_SUMMARY_TAGS = 3;

    @Logging(logArguments = true)
    public Page<ServerInfoResponse> getAllServer(
            Boolean requiresPremium,
            ServerType serverType,
            ServerPlatform serverPlatform,
            List<String> tags,
            Pageable pageable) {
        Specification<Server> specification =
                getServerSpecification(null, requiresPremium, serverType, serverPlatform, tags);
        return findServers(specification, pageable);
    }

    @Logging(logArguments = true)
    public Page<ServerInfoResponse> searchServer(
            String searchQuery,
            Boolean requiresPremium,
            ServerType serverType,
            ServerPlatform serverPlatform,
            List<String> tags,
            Pageable pageable) {
        Specification<Server> specification =
                getServerSpecification(
                        searchQuery, requiresPremium, serverType, serverPlatform, tags);
        return findServers(specification, pageable);
    }

    private Page<ServerInfoResponse> findServers(
            Specification<Server> specification, Pageable pageable) {
        return toInfoPage(serverRepository.findAll(specification, pageable));
    }

    /** Maps a page of servers with one batched lookup per related table instead of one per row. */
    private Page<ServerInfoResponse> toInfoPage(Page<Server> page) {
        List<Server> content = page.getContent();
        if (content.isEmpty())
            return new PageImpl<>(List.of(), page.getPageable(), page.getTotalElements());

        List<String> ids = content.stream().map(Server::getId).toList();
        Map<String, String> usernames =
                authUserQuery.getUsernamesByAuthUserIds(
                        content.stream().map(Server::getUserId).collect(Collectors.toSet()));

        Map<String, List<String>> tags = new HashMap<>();
        for (ServerTag tag : tagRepository.findAllByServerIdInOrderByCreatedAtAsc(ids)) {
            List<String> serverTags =
                    tags.computeIfAbsent(tag.getServer().getId(), id -> new ArrayList<>());
            if (serverTags.size() < MAX_SUMMARY_TAGS) serverTags.add(tag.getTag());
        }

        // A server may store versions for both editions; only its own platform's primary counts
        // HashMap, not Collectors.toMap: legacy rows may have no platform (null value)
        Map<String, ServerPlatform> platforms = new HashMap<>();
        content.forEach(server -> platforms.put(server.getId(), server.getPlatform()));
        Map<String, String> primaryVersions = new HashMap<>();
        if (platforms.containsValue(ServerPlatform.JAVA)) {
            javaMcVersionRepository
                    .findAllByServerIdInAndIsPrimaryMcVersionTrue(ids)
                    .forEach(
                            v -> {
                                String id = v.getServer().getId();
                                if (platforms.get(id) == ServerPlatform.JAVA)
                                    primaryVersions.put(id, v.getMcVersion());
                            });
        }
        if (platforms.containsValue(ServerPlatform.BEDROCK)) {
            bedrockMcVersionRepository
                    .findAllByServerIdInAndIsPrimaryMcVersionTrue(ids)
                    .forEach(
                            v -> {
                                String id = v.getServer().getId();
                                if (platforms.get(id) == ServerPlatform.BEDROCK)
                                    primaryVersions.put(id, v.getMcVersion());
                            });
        }

        return page.map(
                server -> {
                    String username = usernames.get(server.getUserId());
                    if (username == null)
                        throw new UserNotFound("User not found for server: " + server.getId());
                    return ServerInfoResponse.builder()
                            .username(username)
                            .servername(server.getServername())
                            .requiresPremium(server.getRequiresPremium())
                            .avatarUrl(
                                    resourceStorage.createSignedGetUrl(server.getServerAvatarId()))
                            .bannerUrl(
                                    resourceStorage.createSignedGetUrl(server.getServerBannerId()))
                            .summary(server.getSummary())
                            .starCount(server.getStarCount())
                            .visibility(server.getVisibility())
                            .serverType(server.getType())
                            .serverPlatform(server.getPlatform())
                            .tags(List.copyOf(tags.getOrDefault(server.getId(), List.of())))
                            .primaryVersion(primaryVersions.get(server.getId()))
                            .build();
                });
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
        Server server =
                serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));

        if (body.avatarResourceId().isPresent()) {
            if (server.getServerAvatarId() != null) {
                eventPublisher.publishEvent(
                        new ServerAvatarDeletedEvent(server.getServerAvatarId()));
            }
            if (body.avatarResourceId().isNull()) {
                server.setServerAvatarId(null);
            } else {
                if (!resourceStorage.isResourceIdExist(body.avatarResourceId().get())) {
                    throw new InvalidResourceId(
                            "Resource ID does not exist: " + body.avatarResourceId().get());
                }
                eventPublisher.publishEvent(
                        new ServerAvatarUpdatedEvent(body.avatarResourceId().get()));
                server.setServerAvatarId(body.avatarResourceId().get());
            }
        }

        if (body.bannerResourceId().isPresent()) {
            if (server.getServerBannerId() != null) {
                eventPublisher.publishEvent(
                        new ServerBannerDeletedEvent(server.getServerBannerId()));
            }
            if (body.bannerResourceId().isNull()) {
                server.setServerBannerId(null);
            } else {
                if (!resourceStorage.isResourceIdExist(body.bannerResourceId().get())) {
                    throw new InvalidResourceId(
                            "Resource ID does not exist: " + body.bannerResourceId().get());
                }
                eventPublisher.publishEvent(
                        new ServerBannerUpdatedEvent(body.bannerResourceId().get()));
                server.setServerBannerId(body.bannerResourceId().get());
            }
        }

        body.bedrockAddress().ifPresent(server::setBedrockAddress);
        body.javaAddress().ifPresent(server::setJavaAddress);
        if (body.requiresPremium() != null) {
            server.setRequiresPremium(body.requiresPremium());
        }
        body.summary().ifPresent(server::setSummary);
        body.bio().ifPresent(server::setBio);
        serverRepository.save(server);
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
            String searchQuery,
            Boolean requiresPremium,
            ServerType serverType,
            ServerPlatform serverPlatform,
            List<String> tags) {
        // Collect predicates here and and() them: Hibernate's getExpressions() returns a copy,
        // so anything added to conjunction().getExpressions() is silently lost
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isTrue(root.get("visibility")));
            if (searchQuery != null && !searchQuery.isBlank()) {
                String searchPattern = "%" + searchQuery.trim().toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("servername")), searchPattern));
            }
            if (requiresPremium != null) {
                predicates.add(criteriaBuilder.equal(root.get("requiresPremium"), requiresPremium));
            }
            if (serverType != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), serverType));
            }
            if (serverPlatform != null) {
                predicates.add(criteriaBuilder.equal(root.get("platform"), serverPlatform));
            }
            if (tags != null && !tags.isEmpty()) {
                Subquery<Server> subquery = query.subquery(Server.class);
                Root<ServerTag> subRoot = subquery.from(ServerTag.class);
                subquery.select(subRoot.get("server"));
                subquery.where(subRoot.get("tag").in(tags));
                predicates.add(criteriaBuilder.in(root).value(subquery));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public Page<ServerInfoResponse> getServerByUser(
            String userId, String authenticatedUserId, Pageable pageable) {
        Page<Server> servers =
                userId.equals(authenticatedUserId)
                        ? serverRepository.findAllByUserId(userId, pageable)
                        : serverRepository.findAllByUserIdAndVisibilityIsTrue(userId, pageable);
        return toInfoPage(servers);
    }
}
