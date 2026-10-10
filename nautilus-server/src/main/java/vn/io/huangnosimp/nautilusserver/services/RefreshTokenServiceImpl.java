package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.InvalidRefreshToken;
import vn.io.huangnosimp.nautilusserver.models.RefreshToken;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.RefreshTokenRepository;
import vn.io.huangnosimp.nautilusserver.services.interfaces.RefreshTokenService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;
    
    @Override
    @Transactional
    public String generateRefreshToken(User user) {
        Instant expirationTime = Instant.now().plusMillis(refreshTokenExpirationMs);
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiredAt(expirationTime)
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    @Transactional
    public void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public String getOwnerId(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));
        return token.getUser().getId();
    }

    @Override
    @Transactional
    public void validateRefreshToken(String refreshToken) throws InvalidRefreshToken {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));

        if (token.getExpiredAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new InvalidRefreshToken("Refresh token has expired");
        }
    }
}
