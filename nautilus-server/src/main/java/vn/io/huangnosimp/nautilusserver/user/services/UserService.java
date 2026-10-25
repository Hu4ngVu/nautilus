package vn.io.huangnosimp.nautilusserver.user.services;

import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;

public interface UserService {
    UserInfoResponse getUserInfo(String username);

    void updateUserInfo(String username, UpdateUserInfoRequest updateUserInfoRequest);

    void deleteUserAvatar(String username);
}
