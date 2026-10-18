package vn.io.huangnosimp.nautilusserver.services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.InvalidRefreshToken;
import vn.io.huangnosimp.nautilusserver.models.RefreshToken;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.RefreshTokenRepository;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;
    
    @Override
    @Transactional
    public String generateRefreshToken(User user) {
        log.debug("Generating refresh token for user: {}", user.getUsername());
        Instant expirationTime = Instant.now().plusMillis(refreshTokenExpirationMs);
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiredAt(expirationTime)
                .build();
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token generated for user: {}, token", user.getUsername());
        return token;
    }

    @Override
    @Transactional
    public void deleteRefreshToken(User user) {
        log.debug("Deleting refresh token for user: {}", user.getUsername());
        refreshTokenRepository.deleteByUser(user);
        log.info("Refresh token deleted for user: {}", user.getUsername());
    }

    @Override
    public String getOwnerId(String refreshToken) {
        log.debug("Getting owner ID for refresh token: {}", refreshToken);
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));
        log.info("Owner ID for refresh token {} is {}", refreshToken, token.getUser().getId());
        return token.getUser().getId();
    }

    @Override
    @Transactional
    public void validateRefreshToken(String refreshToken) throws InvalidRefreshToken {
        log.debug("Validating refresh token: {}", refreshToken);
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));
        if (token.getExpiredAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token has expired: {}", refreshToken);
            throw new InvalidRefreshToken("Refresh token has expired");
        }
        log.info("Refresh token is valid: {}", refreshToken);
    }
}
