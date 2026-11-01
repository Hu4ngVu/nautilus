package vn.io.huangnosimp.nautilusserver.authentication.services;

import org.springframework.security.oauth2.jwt.Jwt;

public interface TokenBlacklistService {
    void addToken(Jwt token);

    boolean isBlacklisted(Jwt token);
}
