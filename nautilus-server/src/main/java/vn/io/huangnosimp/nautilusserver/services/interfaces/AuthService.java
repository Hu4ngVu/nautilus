package vn.io.huangnosimp.nautilusserver.services.interfaces;

import vn.io.huangnosimp.nautilusserver.models.User;

public interface AuthService {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User registerUser(User user);
}
