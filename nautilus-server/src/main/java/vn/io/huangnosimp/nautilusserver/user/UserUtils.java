package vn.io.huangnosimp.nautilusserver.user;

import java.util.Optional;

public interface UserUtils {
    Optional<UserData> getUserDataByAuthUserId(String authUserId);
}
