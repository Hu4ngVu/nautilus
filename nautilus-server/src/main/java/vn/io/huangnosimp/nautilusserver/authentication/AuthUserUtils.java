package vn.io.huangnosimp.nautilusserver.authentication;

import java.util.Optional;

public interface AuthUserUtils {
    Optional<String> getAuthUserIdByUsername(String username);

    void changeUsername(String userId, String newUsername);
}
