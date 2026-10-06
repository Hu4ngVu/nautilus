package vn.io.huangnosimp.nautilusserver.services.interfaces;

import vn.io.huangnosimp.nautilusserver.dto.request.auth.LoginUser;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.RegisterUser;
import vn.io.huangnosimp.nautilusserver.dto.response.auth.Login;

public interface AuthService {
    void registerUser(RegisterUser registerUser);
    Login loginUser(LoginUser loginUser);
}
