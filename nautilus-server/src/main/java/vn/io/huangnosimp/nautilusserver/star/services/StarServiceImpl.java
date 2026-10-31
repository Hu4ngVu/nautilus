package vn.io.huangnosimp.nautilusserver.star.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.StarNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
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

@Service
@RequiredArgsConstructor
public class StarServiceImpl implements StarService {
    private final AuthUserUtils authUserUtils;
    private final UserUtils userUtils;
    private final ServerUtils serverUtils;
    private final StarRepository starRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public Page<UserStaredInfo> getUserStaredInfo(
            String username, String servername, Pageable pageable) {
        var serverId = getServerId(getUserId(username), servername);
        return starRepository
                .findAllByServerId(serverId, pageable)
                .map(
                        star -> {
                            var userData = getUserData(star.getUserId());
                            return UserStaredInfo.builder()
                                    .username(userData.username())
                                    .avatarUrl(userData.avatarUrl())
                                    .build();
                        });
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public Page<ServerStaredInfo> getServerStaredInfo(String username, Pageable pageable) {
        var userId = getUserId(username);
        return starRepository
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
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void addStar(String authenticationName, String username, String servername) {
        var userId = getUserId(authenticationName);
        var ownerId = getUserId(username);
        var serverId = getServerId(ownerId, servername);
        starRepository.save(Star.builder().userId(userId).serverId(serverId).build());
        eventPublisher.publishEvent(new AddStarEvent(serverId));
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteStar(String authenticationName, String username, String servername) {
        var userId = getUserId(authenticationName);
        var ownerId = getUserId(username);
        var serverId = getServerId(ownerId, servername);
        var star =
                starRepository
                        .findByUserIdAndServerId(userId, serverId)
                        .orElseThrow(() -> new StarNotFound("Star not found"));
        starRepository.delete(star);
        eventPublisher.publishEvent(new DeleteStarEvent(serverId));
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
