package vn.io.huangnosimp.nautilusserver.auth.services;

import vn.io.huangnosimp.nautilusserver.auth.exceptions.InvalidRefreshToken;
import vn.io.huangnosimp.nautilusserver.auth.models.AuthUser;

public interface RefreshTokenService {
    String generateRefreshToken(AuthUser user);

    void deleteRefreshToken(AuthUser user);

    void validateRefreshToken(String refreshToken) throws InvalidRefreshToken;

    String getOwnerId(String refreshToken);
}
