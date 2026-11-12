package vn.io.huangnosimp.nautilusserver.star.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.api.AuthUserQuery;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.api.UserQuery;
import vn.io.huangnosimp.nautilusserver.events.AddStarEvent;
import vn.io.huangnosimp.nautilusserver.events.DeleteStarEvent;
import vn.io.huangnosimp.nautilusserver.events.ServerData;
import vn.io.huangnosimp.nautilusserver.events.UserData;
import vn.io.huangnosimp.nautilusserver.exceptions.StarNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.star.dto.ServerStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.dto.UserStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.models.Star;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;

@Service
@RequiredArgsConstructor
public class StarService {
    private final UserQuery userQuery;
    private final ServerQuery serverQuery;
    private final AuthUserQuery authUserQuery;
    private final StarRepository starRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public Page<UserStaredInfo> getUserStaredInfo(String serverId, Pageable pageable) {
        Page<Star> stars = starRepository.findAllByServerId(serverId, pageable);
        Map<String, UserData> users =
                userQuery.getUserDataByAuthUserIds(
                        stars.getContent().stream().map(Star::getUserId).toList());
        return stars.map(
                star -> {
                    UserData userData = users.get(star.getUserId());
                    if (userData == null) throw new UserNotFound(star.getUserId());
                    return UserStaredInfo.builder()
                            .username(userData.username())
                            .avatarUrl(userData.avatarUrl())
                            .build();
                });
    }

    /**
     * Stars on servers that are currently public. Visibility lives in the server module, so the
     * public subset is resolved through {@link ServerQuery} before paging instead of joining across
     * module boundaries.
     */
    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public Page<ServerStaredInfo> getServerStaredInfo(String userId, Pageable pageable) {
        List<String> publicServerIds =
                serverQuery.filterPublicServerIds(starRepository.findServerIdsByUserId(userId));
        if (publicServerIds.isEmpty()) return Page.empty(pageable);

        Page<Star> stars =
                starRepository.findAllByUserIdAndServerIdIn(userId, publicServerIds, pageable);
        Map<String, ServerData> servers =
                serverQuery.getPublicServerDataByServerIds(
                        stars.getContent().stream().map(Star::getServerId).toList());
        Map<String, String> owners =
                authUserQuery.getUsernamesByAuthUserIds(
                        servers.values().stream()
                                .map(ServerData::ownerUserId)
                                .collect(Collectors.toSet()));

        // A server may turn private between the filter and the page query; drop it rather than fail
        List<ServerStaredInfo> content =
                stars.getContent().stream()
                        .map(star -> servers.get(star.getServerId()))
                        .filter(
                                server ->
                                        server != null && owners.containsKey(server.ownerUserId()))
                        .map(
                                server ->
                                        ServerStaredInfo.builder()
                                                .username(owners.get(server.ownerUserId()))
                                                .servername(server.servername())
                                                .serverAvatarUrl(server.serverAvatarUrl())
                                                .serverTotalStar(server.serverTotalStar())
                                                .build())
                        .toList();
        return new PageImpl<>(content, pageable, stars.getTotalElements());
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void addStar(String userId, String serverId) {
        starRepository.save(Star.builder().userId(userId).serverId(serverId).build());
        eventPublisher.publishEvent(new AddStarEvent(serverId));
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteStar(String userId, String serverId) {
        var star =
                starRepository
                        .findByUserIdAndServerId(userId, serverId)
                        .orElseThrow(() -> new StarNotFound("Star not found"));
        starRepository.delete(star);
        eventPublisher.publishEvent(new DeleteStarEvent(serverId));
    }
}
