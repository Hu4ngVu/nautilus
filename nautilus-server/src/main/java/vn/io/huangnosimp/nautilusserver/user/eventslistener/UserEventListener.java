package vn.io.huangnosimp.nautilusserver.user.eventslistener;

import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.server.ServerCreatedEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserEventListener {
    private final UserRepository userRepository;

    @ApplicationModuleListener
    public void on(ServerCreatedEvent event) {
        User user =
                userRepository
                        .findByAuthUserId(event.userId())
                        .orElseThrow(() -> new UserNotFound("User not found: " + event.userId()));
        userRepository.increaseServerCount(user.getAuthUserId(), 1);
    }

    @ApplicationModuleListener
    public void on(ServerDeletedEvent event) {
        User user =
                userRepository
                        .findByAuthUserId(event.userId())
                        .orElseThrow(() -> new UserNotFound("User not found: " + event.userId()));
        userRepository.decreaseServerCount(user.getAuthUserId(), 1);
    }
}
