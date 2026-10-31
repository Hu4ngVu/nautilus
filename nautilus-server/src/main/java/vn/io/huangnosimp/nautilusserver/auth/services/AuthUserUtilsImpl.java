package vn.io.huangnosimp.nautilusserver.auth.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.auth.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class AuthUserUtilsImpl implements AuthUserUtils {
    private final AuthUserRepository authUserRepository;

    @Logging(level = Level.DEBUG)
    @Override
    public Optional<String> getAuthUserIdByUsername(String username) {
        return authUserRepository.findIdByUsername(username);
    }
}
