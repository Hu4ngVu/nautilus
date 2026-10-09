package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.services.interfaces.TokenBlacklistService;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void addToken(Jwt token) {
        Instant expireAt = token.getExpiresAt();
        long remainingMs = Duration.between(Instant.now(), expireAt).toMillis();
        if (remainingMs <= 0) {
            log.warn("Token {} is already expired, not adding to blacklist", token.getTokenValue());
            return;
        }
        redisTemplate.opsForValue().set(token.getTokenValue(), "blacklisted", Duration.ofMillis(remainingMs));
        log.info("Token {} added to blacklist with expiration in {} ms", token.getTokenValue(), remainingMs);
    }

    @Override
    public boolean isBlacklisted(Jwt token) {
        boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(token.getTokenValue()));
        if (isBlacklisted) {
            log.info("Token {} is blacklisted", token.getTokenValue());
        } else {
            log.info("Token {} is not blacklisted", token.getTokenValue());
        }
        return isBlacklisted;
    }
}
