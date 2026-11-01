package vn.io.huangnosimp.nautilusserver.authentication.services;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RCuckooFilter;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class UsernameEmailCheckingServiceImpl implements UsernameEmailCheckingService {
    @Qualifier("usernameCuckooFilter")
    private final RCuckooFilter<String> usernameCuckooFilter;

    @Qualifier("emailCuckooFilter")
    private final RCuckooFilter<String> emailCuckooFilter;

    private final AuthUserRepository authUserRepository;

    @Override
    @Logging(level = Level.DEBUG)
    public boolean isUsernameExists(String username) {
        if (usernameCuckooFilter.exists(username)) {
            return authUserRepository.existsByUsername(username);
        }
        return false;
    }

    @Override
    @Logging(level = Level.DEBUG)
    public boolean isEmailExists(String email) {
        if (emailCuckooFilter.exists(email)) {
            return authUserRepository.existsByEmail(email);
        }
        return false;
    }
}
