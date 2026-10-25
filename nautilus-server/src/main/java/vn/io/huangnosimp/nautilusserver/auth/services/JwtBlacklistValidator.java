package vn.io.huangnosimp.nautilusserver.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlacklistValidator implements OAuth2TokenValidator<Jwt> {
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @NullMarked
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.debug("Token {} is blacklisted", token.getTokenValue());
            OAuth2Error error =
                    new OAuth2Error("invalid_token", "The token has been blacklisted", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        log.debug("Token {} is valid", token.getTokenValue());
        return OAuth2TokenValidatorResult.success();
    }
}
