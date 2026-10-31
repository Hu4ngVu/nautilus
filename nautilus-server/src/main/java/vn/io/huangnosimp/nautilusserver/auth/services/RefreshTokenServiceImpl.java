package vn.io.huangnosimp.nautilusserver.auth.services;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.auth.models.RefreshToken;
import vn.io.huangnosimp.nautilusserver.auth.repositories.RefreshTokenRepository;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidRefreshToken;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Logging(level = Level.DEBUG)
    @Transactional(rollbackFor = Exception.class)
    public String generateRefreshToken(AuthUser user) {
        Instant expirationTime = Instant.now().plusMillis(refreshTokenExpirationMs);
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(token)
                        .authUser(user)
                        .expiredAt(expirationTime)
                        .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    @Logging(level = Level.DEBUG)
    @Transactional(rollbackFor = Exception.class)
    public void deleteRefreshToken(AuthUser user) {
        refreshTokenRepository.deleteByAuthUser(user);
    }

    @Override
    @Logging(level = Level.DEBUG)
    public String getOwnerId(String refreshToken) {
        RefreshToken token =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));
        return token.getAuthUser().getId();
    }

    @Override
    @Logging(level = Level.DEBUG)
    @Transactional(rollbackFor = Exception.class)
    public void validateRefreshToken(String refreshToken) throws InvalidRefreshToken {
        RefreshToken token =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));
        if (token.getExpiredAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new InvalidRefreshToken("Refresh token has expired");
        }
    }
}
