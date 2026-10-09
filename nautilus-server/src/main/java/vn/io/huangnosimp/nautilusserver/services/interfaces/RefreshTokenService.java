package vn.io.huangnosimp.nautilusserver.services.interfaces;

import vn.io.huangnosimp.nautilusserver.exceptions.auth.InvalidRefreshToken;
import vn.io.huangnosimp.nautilusserver.models.User;

public interface RefreshTokenService {
    String generateRefreshToken(User user);
    void deleteRefreshToken(User user);
    void validateRefreshToken(String refreshToken) throws InvalidRefreshToken;
    String getOwnerId(String refreshToken);
}
