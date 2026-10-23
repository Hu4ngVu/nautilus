package vn.io.huangnosimp.nautilusserver.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.auth.repositories.AuthUserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthUserUtilsImpl implements AuthUserUtils {
    private final AuthUserRepository authUserRepository;

    @Override
    public Optional<String> getAuthUserIdByUsername(String username) {
        return authUserRepository.findIdByUsername(username);
    }
}
