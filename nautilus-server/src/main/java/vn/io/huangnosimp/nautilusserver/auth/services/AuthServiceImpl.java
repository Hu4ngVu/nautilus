package vn.io.huangnosimp.nautilusserver.auth.services;

import org.redisson.api.RCuckooFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import vn.io.huangnosimp.nautilusserver.auth.UserRegisteredEvent;
import vn.io.huangnosimp.nautilusserver.auth.constants.Provider;
import vn.io.huangnosimp.nautilusserver.auth.constants.Role;
import vn.io.huangnosimp.nautilusserver.auth.dto.results.TokenResult;
import vn.io.huangnosimp.nautilusserver.auth.exceptions.AuthUserNotFound;
import vn.io.huangnosimp.nautilusserver.auth.exceptions.DuplicateEmail;
import vn.io.huangnosimp.nautilusserver.auth.exceptions.DuplicateUsername;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.dto.requests.LoginRequest;
import vn.io.huangnosimp.nautilusserver.auth.dto.requests.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.auth.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.auth.repositories.AuthUserRepository;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RCuckooFilter<String> usernameCuckooFilter;
    private final RCuckooFilter<String> emailCuckooFilter;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private String generateAccessToken(AuthUser authUser) {
        Instant now = Instant.now();
        String role = authUser
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                        .issuer("nautilus-server")
                        .issuedAt(now)
                        .expiresAt(now.plusMillis(jwtExpirationMs))
                        .subject(authUser.getUsername())
                        .claim("role", role)
                        .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(RegisterRequest registerRequest) {
        log.debug("registering user: {}", registerRequest.username());
        if (authUserRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicateUsername("Username is already taken: " + registerRequest.username());
        }
        if (authUserRepository.existsByEmail(registerRequest.email())) {
            throw  new DuplicateEmail("Email is already taken: " + registerRequest.email());
        }
        AuthUser authUser = AuthUser.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .build();
        authUserRepository.save(authUser);
        usernameCuckooFilter.add(authUser.getUsername());
        emailCuckooFilter.add(authUser.getEmail());
        eventPublisher.publishEvent(new UserRegisteredEvent(authUser.getId(), authUser.getUsername()));
        log.info("registered user: {}", authUser.getUsername());
    }

    @Override
    public TokenResult loginUser(LoginRequest loginRequest) {
        log.debug("logging in user: {}", loginRequest.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (authUser == null) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String accessToken = generateAccessToken(authUser);
        String refreshToken = refreshTokenService.generateRefreshToken(authUser);
        log.info("user {} logged in successfully", loginRequest.username());
        return new TokenResult(accessToken, refreshToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenResult refreshToken(String refreshToken) {
        log.debug("refreshing token");
        refreshTokenService.validateRefreshToken(refreshToken);
        String ownerId = refreshTokenService.getOwnerId(refreshToken);
        AuthUser authUser = authUserRepository.findById(ownerId)
                .orElseThrow(() -> new AuthUserNotFound("User not found: " + ownerId));
        String accessToken = generateAccessToken(authUser);
        refreshTokenService.deleteRefreshToken(authUser);
        log.info("user {} refreshed access token successfully", authUser.getUsername());
        String newRefreshToken = refreshTokenService.generateRefreshToken(authUser);
        log.info("user {} generated new refresh token successfully", authUser.getUsername());
        return new TokenResult(accessToken, newRefreshToken);
    }

    @Override
    public void logoutUser(Jwt token) {
        log.debug("logging out user: {}", token.getSubject());
        AuthUser authUser = authUserRepository.findByUsername(token.getSubject())
                .orElseThrow(() -> new AuthUserNotFound("User not found: " + token.getSubject()));
        tokenBlacklistService.addToken(token);
        refreshTokenService.deleteRefreshToken(authUser);
        SecurityContextHolder.clearContext();
        log.info("user {} logged out successfully", token.getSubject());
    }
}
