package vn.io.huangnosimp.nautilusserver.authentication.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.authentication.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class AuthUserUtilsImpl implements AuthUserUtils {
    private final AuthUserRepository authUserRepository;

    @Override
    @Logging(level = Level.DEBUG)
    public Optional<String> getAuthUserIdByUsername(String username) {
        return authUserRepository.findIdByUsername(username);
    }

    @Override
    @Logging(level = Level.DEBUG)
    public void changeUsername(String userId, String newUsername) {
        authUserRepository.updateUsernameById(userId, newUsername);
    }
}
