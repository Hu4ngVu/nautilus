package vn.io.huangnosimp.nautilusserver.services.interfaces;

import org.springframework.security.core.userdetails.UserDetails;
import vn.io.huangnosimp.nautilusserver.constants.Role;
import vn.io.huangnosimp.nautilusserver.models.User;

public interface JwtService {
    String generateToken(User user);

    String extractUsername(String token);

    Role extractRole(String token);

    boolean isValid(String token, UserDetails user);
}
