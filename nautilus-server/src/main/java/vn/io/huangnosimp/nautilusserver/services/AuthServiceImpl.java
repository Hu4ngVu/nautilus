package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User registerUser(User user) {
        return userRepository.save(user);
    }
}
