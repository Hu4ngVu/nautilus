package vn.io.huangnosimp.nautilusserver.auth;

import java.util.Optional;

public interface AuthUserUtils {
    Optional<String> getAuthUserIdByUsername(String username);
}
