package vn.io.huangnosimp.nautilusserver.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void addToken(Jwt token) {
        log.debug("Adding token {} to blacklist", token.getTokenValue());
        Instant expireAt = token.getExpiresAt();
        long remainingMs = Duration.between(Instant.now(), expireAt).toMillis();
        if (remainingMs <= 0) {
            log.warn("Token {} is already expired, not adding to blacklist", token.getTokenValue());
            return;
        }
        redisTemplate.opsForValue().set(token.getTokenValue(), "blacklisted", Duration.ofMillis(remainingMs));
        log.info("Token added to blacklist");
    }

    @Override
    public boolean isBlacklisted(Jwt token) {
        log.debug("Checking if token {} is blacklisted", token.getTokenValue());
        boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(token.getTokenValue()));
        if (isBlacklisted) {
            log.info("Token is blacklisted");
        } else {
            log.info("Token is not blacklisted");
        }
        return isBlacklisted;
    }
}
