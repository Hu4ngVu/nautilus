package vn.io.huangnosimp.nautilusserver.authentication.services;

import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidRefreshToken;

public interface RefreshTokenService {
    String generateRefreshToken(AuthUser user);

    void deleteRefreshToken(AuthUser user);

    void validateRefreshToken(String refreshToken) throws InvalidRefreshToken;

    String getOwnerId(String refreshToken);
}
