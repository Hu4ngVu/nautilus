package vn.io.huangnosimp.nautilusserver.auth.services;

import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.huangnosimp.nautilusserver.auth.dto.requests.LoginRequest;
import vn.io.huangnosimp.nautilusserver.auth.dto.requests.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.auth.dto.results.TokenResult;

public interface AuthService {
    void registerUser(RegisterRequest registerRequest);

    TokenResult loginUser(LoginRequest loginRequest);

    void logoutUser(Jwt token);

    TokenResult refreshToken(String refreshToken);
}
