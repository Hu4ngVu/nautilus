package vn.io.huangnosimp.nautilusserver.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface AuthUserQuery {
    Optional<String> getAuthUserIdByUsername(String username);

    /** Resolves many usernames in one lookup; ids without a user are absent from the map. */
    Map<String, String> getUsernamesByAuthUserIds(Collection<String> authUserIds);
}
