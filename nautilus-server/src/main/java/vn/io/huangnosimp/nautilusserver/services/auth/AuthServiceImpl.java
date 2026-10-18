package vn.io.huangnosimp.nautilusserver.services.auth;

import org.redisson.api.RCuckooFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import vn.io.huangnosimp.nautilusserver.constants.Provider;
import vn.io.huangnosimp.nautilusserver.constants.Role;
import vn.io.huangnosimp.nautilusserver.dto.result.TokenResult;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.DuplicateEmail;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.DuplicateUsername;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.dto.request.LoginRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.models.UserDetailsWrapper;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RCuckooFilter<String> usernameCuckooFilter;
    private final RCuckooFilter<String> emailCuckooFilter;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private String generateAccessToken(UserDetailsWrapper userDetailsWrapper) {
        Instant now = Instant.now();
        String role = userDetailsWrapper
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                        .issuer("nautilus-server")
                        .issuedAt(now)
                        .expiresAt(now.plusMillis(jwtExpirationMs))
                        .subject(userDetailsWrapper.getUsername())
                        .claim("role", role)
                        .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        log.debug("registering user: {}", registerRequest.username());
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicateUsername("Username is already taken: " + registerRequest.username());
        }
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw  new DuplicateEmail("Email is already taken: " + registerRequest.email());
        }
        User user = User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .serverCount(0)
                .avatarKey(null)
                .bio(null)
                .enable(true)
                .build();
        userRepository.save(user);
        usernameCuckooFilter.add(user.getUsername());
        emailCuckooFilter.add(user.getEmail());
        log.info("registered user: {}", user.getUsername());
    }

    @Override
    public TokenResult loginUser(LoginRequest loginRequest) {
        log.debug("logging in user: {}", loginRequest.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        UserDetailsWrapper userDetailsWrapper = (UserDetailsWrapper) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (userDetailsWrapper == null) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String accessToken = generateAccessToken(userDetailsWrapper);
        String refreshToken = refreshTokenService.generateRefreshToken(userDetailsWrapper.user());
        log.info("user {} logged in successfully", loginRequest.username());
        return new TokenResult(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenResult refreshToken(String refreshToken) {
        log.debug("refreshing token");
        refreshTokenService.validateRefreshToken(refreshToken);
        String ownerId = refreshTokenService.getOwnerId(refreshToken);
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFound("User not found: " + ownerId));
        String accessToken = generateAccessToken(new UserDetailsWrapper(user));
        refreshTokenService.deleteRefreshToken(user);
        log.info("user {} refreshed access token successfully", user.getUsername());
        String newRefreshToken = refreshTokenService.generateRefreshToken(user);
        log.info("user {} generated new refresh token successfully", user.getUsername());
        return new TokenResult(accessToken, newRefreshToken);
    }

    @Override
    public void logoutUser(Jwt token) {
        log.debug("logging out user: {}", token.getSubject());
        User user = userRepository.findByUsername(token.getSubject())
                .orElseThrow(() -> new UserNotFound("User not found: " + token.getSubject()));
        tokenBlacklistService.addToken(token);
        refreshTokenService.deleteRefreshToken(user);
        SecurityContextHolder.clearContext();
        log.info("user {} logged out successfully", token.getSubject());
    }
}
