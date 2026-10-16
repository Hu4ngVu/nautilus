package vn.io.huangnosimp.nautilusserver.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.UserInfoResponse;

public interface UserService extends UserDetailsService {
    UserInfoResponse getUserInfo(String username);
    void updateUserInfo(String username, UpdateUserInfoRequest updateUserInfoRequest);
}
