package vn.io.huangnosimp.nautilusserver.user.services;

import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;

public interface UserService {
    UserInfoResponse getUserInfo(String authUserId);

    void updateUserInfo(String authUserId, UpdateUserInfoRequest updateUserInfoRequest);

    void deleteUserAvatar(String authUserId);

    void changeUsername(String authUserId, ChangeUsernameRequest request);
}
