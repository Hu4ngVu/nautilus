package vn.io.huangnosimp.nautilusserver.api;

import java.util.Optional;
import vn.io.huangnosimp.nautilusserver.events.UserData;

public interface UserQuery {
    Optional<UserData> getUserDataByAuthUserId(String authUserId);
}
