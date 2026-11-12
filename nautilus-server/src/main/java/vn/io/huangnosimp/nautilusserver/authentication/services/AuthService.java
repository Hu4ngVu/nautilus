package vn.io.huangnosimp.nautilusserver.authentication.services;

import java.time.Instant;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RCuckooFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Provider;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Role;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.ChangePasswordRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.LoginRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.results.TokenResult;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.events.UserRegisteredEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.AuthUserNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateEmail;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidPassword;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class AuthService {
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
        String role =
                authUser.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet =
                JwtClaimsSet.builder()
                        .issuer("nautilus-server")
                        .issuedAt(now)
                        .expiresAt(now.plusMillis(jwtExpirationMs))
                        .subject(authUser.getUsername())
                        .claim("role", role)
                        .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    @Logging
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(RegisterRequest registerRequest) {
        if (authUserRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicateUsername("Username is already taken: " + registerRequest.username());
        }
        if (authUserRepository.existsByEmail(registerRequest.email())) {
            throw new DuplicateEmail("Email is already taken: " + registerRequest.email());
        }
        AuthUser authUser =
                AuthUser.builder()
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
        eventPublisher.publishEvent(
                new UserRegisteredEvent(authUser.getId(), authUser.getUsername()));
    }

    @Logging
    public TokenResult loginUser(LoginRequest loginRequest) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.username(), loginRequest.password()));
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (authUser == null) {
            throw new BadCredentialsException("Invalid userId or password");
        }
        String accessToken = generateAccessToken(authUser);
        String refreshToken = refreshTokenService.generateRefreshToken(authUser);
        return new TokenResult(accessToken, refreshToken);
    }

    @Logging
    @Transactional(rollbackFor = Exception.class)
    public TokenResult refreshToken(String refreshToken) {
        refreshTokenService.validateRefreshToken(refreshToken);
        String ownerId = refreshTokenService.getOwnerId(refreshToken);
        AuthUser authUser =
                authUserRepository
                        .findById(ownerId)
                        .orElseThrow(() -> new AuthUserNotFound("User not found: " + ownerId));
        String accessToken = generateAccessToken(authUser);
        refreshTokenService.deleteRefreshToken(authUser);
        String newRefreshToken = refreshTokenService.generateRefreshToken(authUser);
        return new TokenResult(accessToken, newRefreshToken);
    }

    /** Changes the password, revokes every existing session and issues a fresh token pair. */
    @Logging
    @Transactional(rollbackFor = Exception.class)
    public TokenResult changePassword(Jwt token, ChangePasswordRequest request) {
        AuthUser authUser =
                authUserRepository
                        .findByUsername(token.getSubject())
                        .orElseThrow(
                                () ->
                                        new AuthUserNotFound(
                                                "User not found: " + token.getSubject()));
        if (authUser.getPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), authUser.getPassword())) {
            throw new InvalidPassword("Current password is incorrect");
        }
        authUser.setPassword(passwordEncoder.encode(request.newPassword()));
        authUserRepository.save(authUser);
        tokenBlacklistService.addToken(token);
        refreshTokenService.deleteRefreshToken(authUser);
        return new TokenResult(
                generateAccessToken(authUser), refreshTokenService.generateRefreshToken(authUser));
    }

    @Logging
    public void logoutUser(Jwt token) {
        AuthUser authUser =
                authUserRepository
                        .findByUsername(token.getSubject())
                        .orElseThrow(
                                () ->
                                        new AuthUserNotFound(
                                                "User not found: " + token.getSubject()));
        tokenBlacklistService.addToken(token);
        refreshTokenService.deleteRefreshToken(authUser);
        SecurityContextHolder.clearContext();
    }
}
