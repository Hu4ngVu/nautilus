package vn.io.huangnosimp.nautilusserver.star.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.api.UserQuery;
import vn.io.huangnosimp.nautilusserver.events.AddStarEvent;
import vn.io.huangnosimp.nautilusserver.events.DeleteStarEvent;
import vn.io.huangnosimp.nautilusserver.events.ServerData;
import vn.io.huangnosimp.nautilusserver.events.UserData;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
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
    private final StarRepository starRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public Page<UserStaredInfo> getUserStaredInfo(String serverId, Pageable pageable) {
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

    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public Page<ServerStaredInfo> getServerStaredInfo(String userId, Pageable pageable) {
        return starRepository
                .findAllByUserId(userId, pageable)
                .map(
                        star -> {
                            var serverData = getServerData(star.getServerId());
                            var userdata = getUserData(userId);
                            return ServerStaredInfo.builder()
                                    .username(userdata.username())
                                    .servername(serverData.servername())
                                    .serverAvatarUrl(serverData.serverAvatarUrl())
                                    .serverTotalStar(serverData.serverTotalStar())
                                    .build();
                        });
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

    private UserData getUserData(String userId) {
        return userQuery
                .getUserDataByAuthUserId(userId)
                .orElseThrow(() -> new UserNotFound(userId));
    }

    private ServerData getServerData(String serverId) {
        return serverQuery
                .getPublicServerDataByServerId(serverId)
                .orElseThrow(() -> new ServerNotFound(serverId));
    }
}
