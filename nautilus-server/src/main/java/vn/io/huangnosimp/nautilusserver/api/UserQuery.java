package vn.io.huangnosimp.nautilusserver.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import vn.io.huangnosimp.nautilusserver.events.UserData;

public interface UserQuery {
    Optional<UserData> getUserDataByAuthUserId(String authUserId);

    Map<String, UserData> getUserDataByAuthUserIds(Collection<String> authUserIds);
}
