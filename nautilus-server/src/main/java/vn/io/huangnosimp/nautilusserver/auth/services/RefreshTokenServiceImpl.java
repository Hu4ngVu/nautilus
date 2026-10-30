package vn.io.huangnosimp.nautilusserver.auth.services;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.auth.models.RefreshToken;
import vn.io.huangnosimp.nautilusserver.auth.repositories.RefreshTokenRepository;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidRefreshToken;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateRefreshToken(AuthUser user) {
        log.debug("Generating refresh token for user: {}", user.getUsername());
        Instant expirationTime = Instant.now().plusMillis(refreshTokenExpirationMs);
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(token)
                        .authUser(user)
                        .expiredAt(expirationTime)
                        .build();
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token generated for user: {}", user.getUsername());
        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRefreshToken(AuthUser user) {
        log.debug("Deleting refresh token for user: {}", user.getUsername());
        refreshTokenRepository.deleteByAuthUser(user);
        log.info("Refresh token deleted for user: {}", user.getUsername());
    }

    @Override
    public String getOwnerId(String refreshToken) {
        log.debug("Getting owner ID");
        RefreshToken token =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));
        log.info("Got owner ID");
        return token.getAuthUser().getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void validateRefreshToken(String refreshToken) throws InvalidRefreshToken {
        log.debug("Validating refresh token");
        RefreshToken token =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));
        if (token.getExpiredAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token has expired");
            throw new InvalidRefreshToken("Refresh token has expired");
        }
        log.info("Refresh token is valid");
    }
}
