package vn.io.huangnosimp.nautilusserver.services;

import exceptions.auth.DuplicateEmail;
import exceptions.auth.DuplicateUsername;
import exceptions.auth.InvalidPassword;
import exceptions.auth.UserNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.dto.request.LoginUser;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterUser;
import vn.io.huangnosimp.nautilusserver.mapper.AuthMapper;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public void registerUser(RegisterUser registerUser) {
        log.info("registering user: {}", registerUser.username());
        if (userRepository.existsByUsername(registerUser.username())) {
            log.info("Username is already taken: {}", registerUser.username());
            throw new DuplicateUsername("Username is already taken: " + registerUser.username());
        }
        if (userRepository.existsByEmail(registerUser.email())) {
            log.info("Email is already taken: {}", registerUser.email());
            throw  new DuplicateEmail("Email is already taken: " + registerUser.email());
        }
        User user = authMapper.toUser(registerUser);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        log.info("registered user: {}", user.getUsername());
    }

    @Override
    public void loginUser(LoginUser loginUser) {
        log.info("logging in user: {}", loginUser.username());
        Optional<User> userOptional = userRepository.findByUsername(loginUser.username());
        if (userOptional.isEmpty()) {
            log.info("User not found: {}", loginUser.username());
            throw new UserNotFound("User not found: " + loginUser.username());
        }
        if (!passwordEncoder.matches(loginUser.password(), userOptional.get().getPassword())) {
            log.info("Invalid password for user: {}", loginUser.username());
            throw new InvalidPassword("Invalid password for user: " + loginUser.username());
        }
        log.info("user {} logged in successfully", loginUser.username());
    }
}
