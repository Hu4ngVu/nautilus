package vn.io.huangnosimp.nautilusserver.user.eventslistener;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.events.UserRegisteredEvent;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationUserEventListener {
    private final UserRepository userRepository;

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(UserRegisteredEvent event) {
        User user =
                User.builder()
                        .authUserId(event.authUserId())
                        .serverCount(0)
                        .username(event.username())
                        .build();
        userRepository.save(user);
    }
}
