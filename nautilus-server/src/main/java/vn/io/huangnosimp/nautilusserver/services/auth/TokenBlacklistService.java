package vn.io.huangnosimp.nautilusserver.services.auth;

import org.springframework.security.oauth2.jwt.Jwt;

public interface TokenBlacklistService {
    void addToken(Jwt token);
    boolean isBlacklisted(Jwt token);
}
