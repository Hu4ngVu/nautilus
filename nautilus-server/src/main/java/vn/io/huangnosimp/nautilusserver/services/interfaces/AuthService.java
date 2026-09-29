package vn.io.huangnosimp.nautilusserver.services.interfaces;

import vn.io.huangnosimp.nautilusserver.dto.request.LoginUser;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterUser;

public interface AuthService {
    void registerUser(RegisterUser registerUser);
    void loginUser(LoginUser loginUser);
}
