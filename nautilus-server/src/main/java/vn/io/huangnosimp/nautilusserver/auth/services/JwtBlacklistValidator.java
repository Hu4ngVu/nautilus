package vn.io.huangnosimp.nautilusserver.auth.services;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.event.Level;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class JwtBlacklistValidator implements OAuth2TokenValidator<Jwt> {
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @NullMarked
    @Logging(level = Level.DEBUG)
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (tokenBlacklistService.isBlacklisted(token)) {
            OAuth2Error error =
                    new OAuth2Error("invalid_token", "The token has been blacklisted", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
