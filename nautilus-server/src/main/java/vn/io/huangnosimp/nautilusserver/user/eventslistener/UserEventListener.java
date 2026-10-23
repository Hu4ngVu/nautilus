package vn.io.huangnosimp.nautilusserver.user.eventslistener;

import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.auth.UserRegisteredEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerCreatedEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.user.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserEventListener {
    private final UserRepository userRepository;

    @ApplicationModuleListener
    public void on(UserRegisteredEvent event) {
        User user = User.builder()
                .authUserId(event.authUserId())
                .username(event.username())
                .serverCount(0)
                .build();
        userRepository.save(user);
    }

    @ApplicationModuleListener
    public void on(ServerCreatedEvent event) {
        User user = userRepository.findByUsername(event.username())
                .orElseThrow(() -> new UserNotFound("User not found: " + event.username()));
        user.setServerCount(user.getServerCount() + 1);
        userRepository.save(user);
    }

    @ApplicationModuleListener
    public void on(ServerDeletedEvent event) {
        User user = userRepository.findByUsername(event.username())
                .orElseThrow(() -> new UserNotFound("User not found: " + event.username()));
        user.setServerCount(user.getServerCount() - 1);
        userRepository.save(user);
    }
}
