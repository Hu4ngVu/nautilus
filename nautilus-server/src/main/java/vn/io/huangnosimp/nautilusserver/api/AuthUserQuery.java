package vn.io.huangnosimp.nautilusserver.api;

import java.util.Optional;

public interface AuthUserQuery {
    Optional<String> getAuthUserIdByUsername(String username);
}
