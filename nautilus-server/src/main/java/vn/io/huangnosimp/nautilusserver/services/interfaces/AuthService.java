package vn.io.huangnosimp.nautilusserver.services.interfaces;

import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.LoginRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.dto.result.TokenResult;

public interface AuthService {
    void registerUser(RegisterRequest registerRequest);
    TokenResult loginUser(LoginRequest loginRequest);
    void logoutUser(Jwt token);
    TokenResult refreshToken(String refreshToken);
}
