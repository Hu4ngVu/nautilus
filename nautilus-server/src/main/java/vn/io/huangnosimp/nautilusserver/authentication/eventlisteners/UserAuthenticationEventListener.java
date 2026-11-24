package vn.io.huangnosimp.nautilusserver.authentication.eventlisteners;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RCuckooFilter;
import org.slf4j.event.Level;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.events.UsernameUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class UserAuthenticationEventListener {
    private final AuthUserRepository authUserRepository;
    private final RCuckooFilter<String> usernameCuckooFilter;

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(UsernameUpdatedEvent event) {
        authUserRepository.updateUsernameById(event.authUserId(), event.newUsername());
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void updateCuckooFilter(UsernameUpdatedEvent event) {
        usernameCuckooFilter.remove(event.oldUsername());
        usernameCuckooFilter.add(event.newUsername());
    }
}
