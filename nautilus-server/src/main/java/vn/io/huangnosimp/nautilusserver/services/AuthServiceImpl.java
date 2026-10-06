package vn.io.huangnosimp.nautilusserver.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import vn.io.huangnosimp.nautilusserver.constants.Provider;
import vn.io.huangnosimp.nautilusserver.constants.Role;
import vn.io.huangnosimp.nautilusserver.dto.response.auth.Login;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.DuplicateEmail;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.InvalidPassword;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.LoginUser;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.RegisterUser;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;
import vn.io.huangnosimp.nautilusserver.services.interfaces.JwtService;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
        User user = User.builder()
                .username(registerUser.username())
                .email(registerUser.email())
                .password(passwordEncoder.encode(registerUser.password()))
                .provider(Provider.NONE)
                .role(Role.USER)
                .serverCount(0)
                .collectionCount(0)
                .avatarUrl(null)
                .bio(null)
                .enable(true)
                .build();
        userRepository.save(user);
        log.info("registered user: {}", user.getUsername());
    }

    @Override
    public Login loginUser(LoginUser loginUser) {
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

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.username(), loginUser.password()));
        User user = (User) authentication.getPrincipal();
        Login token = new Login(jwtService.generateToken(user));
        log.info("user {} logged in successfully", loginUser.username());
        return token;
    }
}
