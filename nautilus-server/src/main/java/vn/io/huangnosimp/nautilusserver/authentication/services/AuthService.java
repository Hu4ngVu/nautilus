package vn.io.huangnosimp.nautilusserver.authentication.services;

import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.LoginRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.results.TokenResult;

public interface AuthService {
    void registerUser(RegisterRequest registerRequest);

    TokenResult loginUser(LoginRequest loginRequest);

    void logoutUser(Jwt token);

    TokenResult refreshToken(String refreshToken);
}
