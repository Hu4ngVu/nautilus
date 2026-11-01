package vn.io.huangnosimp.nautilusserver.authentication.services;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    @Logging(level = Level.DEBUG)
    public void addToken(Jwt token) {
        Instant expireAt = token.getExpiresAt();
        long remainingMs = Duration.between(Instant.now(), expireAt).toMillis();
        if (remainingMs <= 0) {
            return;
        }
        redisTemplate
                .opsForValue()
                .set(token.getTokenValue(), "blacklisted", Duration.ofMillis(remainingMs));
    }

    @Override
    @Logging(level = Level.DEBUG)
    public boolean isBlacklisted(Jwt token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token.getTokenValue()));
    }
}
