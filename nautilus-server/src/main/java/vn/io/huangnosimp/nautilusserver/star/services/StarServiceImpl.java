package vn.io.huangnosimp.nautilusserver.star.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.StarNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.server.ServerData;
import vn.io.huangnosimp.nautilusserver.server.ServerUtils;
import vn.io.huangnosimp.nautilusserver.star.AddStarEvent;
import vn.io.huangnosimp.nautilusserver.star.DeleteStarEvent;
import vn.io.huangnosimp.nautilusserver.star.dto.ServerStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.dto.UserStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.models.Star;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;
import vn.io.huangnosimp.nautilusserver.user.UserData;
import vn.io.huangnosimp.nautilusserver.user.UserUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class StarServiceImpl implements StarService {
    private final AuthUserUtils authUserUtils;
    private final UserUtils userUtils;
    private final ServerUtils serverUtils;
    private final StarRepository starRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<UserStaredInfo> getUserStaredInfo(
            String username, String servername, Pageable pageable) {
        log.debug("Fetching users who starred server: {} of user: {}", servername, username);
        var serverId = getServerId(getUserId(username), servername);
        var response =
                starRepository
                        .findAllByServerId(serverId, pageable)
                        .map(
                                star -> {
                                    var userData = getUserData(star.getUserId());
                                    return UserStaredInfo.builder()
                                            .username(userData.username())
                                            .avatarUrl(userData.avatarUrl())
                                            .build();
                                });
        log.info(
                "Users who starred server: {} of user: {} fetched successfully",
                servername,
                username);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServerStaredInfo> getServerStaredInfo(String username, Pageable pageable) {
        log.debug("Fetching servers starred by user: {}", username);
        var userId = getUserId(username);
        var response =
                starRepository
                        .findAllByUserId(userId, pageable)
                        .map(
                                star -> {
                                    var serverData = getServerData(star.getServerId());
                                    return ServerStaredInfo.builder()
                                            .username(username)
                                            .servername(serverData.servername())
                                            .serverAvatarUrl(serverData.serverAvatarUrl())
                                            .serverTotalStar(serverData.serverTotalStar())
                                            .build();
                                });
        log.info("Servers starred by user: {} fetched successfully", username);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStar(String authenticationName, String username, String servername) {
        log.debug(
                "Adding star for user: {} on server: {} of user: {}",
                authenticationName,
                servername,
                username);
        var userId = getUserId(authenticationName);
        var ownerId = getUserId(username);
        var serverId = getServerId(ownerId, servername);
        starRepository.save(Star.builder().userId(userId).serverId(serverId).build());
        eventPublisher.publishEvent(new AddStarEvent(serverId));
        log.info(
                "Star added for user: {} on server: {} of user: {}",
                authenticationName,
                servername,
                username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStar(String authenticationName, String username, String servername) {
        log.debug(
                "Deleting star for user: {} on server: {} of user: {}",
                authenticationName,
                servername,
                username);
        var userId = getUserId(authenticationName);
        var ownerId = getUserId(username);
        var serverId = getServerId(ownerId, servername);
        var star =
                starRepository
                        .findByUserIdAndServerId(userId, serverId)
                        .orElseThrow(() -> new StarNotFound("Star not found"));
        starRepository.delete(star);
        eventPublisher.publishEvent(new DeleteStarEvent(serverId));
        log.info(
                "Star deleted for user: {} on server: {} of user: {}",
                authenticationName,
                servername,
                username);
    }

    private String getUserId(String username) {
        return authUserUtils
                .getAuthUserIdByUsername(username)
                .orElseThrow(() -> new UserNotFound(username));
    }

    private String getServerId(String userId, String servername) {
        return serverUtils
                .getPublicServerId(userId, servername)
                .orElseThrow(() -> new ServerNotFound(servername));
    }

    private UserData getUserData(String userId) {
        return userUtils
                .getUserDataByAuthUserId(userId)
                .orElseThrow(() -> new UserNotFound(userId));
    }

    private ServerData getServerData(String serverId) {
        return serverUtils
                .getPublicServerDataByServerId(serverId)
                .orElseThrow(() -> new ServerNotFound(serverId));
    }
}
