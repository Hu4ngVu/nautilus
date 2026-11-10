package vn.io.huangnosimp.nautilusserver.user.eventslistener;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.events.ServerCreatedEvent;
import vn.io.huangnosimp.nautilusserver.events.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class ServerUserEventListener {
    private final UserRepository userRepository;

    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#event.userId()")
    public void on(ServerCreatedEvent event) {
        User user =
                userRepository
                        .findByAuthUserId(event.userId())
                        .orElseThrow(() -> new UserNotFound("User not found: " + event.userId()));
        userRepository.increaseServerCount(user.getAuthUserId(), 1);
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    @CacheEvict(cacheNames = CacheName.SERVER_INFO_CACHE, key = "#event.userId()")
    public void on(ServerDeletedEvent event) {
        User user =
                userRepository
                        .findByAuthUserId(event.userId())
                        .orElseThrow(() -> new UserNotFound("User not found: " + event.userId()));
        if (user.getServerCount() > 0) {
            userRepository.decreaseServerCount(user.getAuthUserId(), 1);
        }
    }
}
