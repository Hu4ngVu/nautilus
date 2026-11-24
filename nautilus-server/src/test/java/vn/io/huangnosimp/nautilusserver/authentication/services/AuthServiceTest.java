package vn.io.huangnosimp.nautilusserver.authentication.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RCuckooFilter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.ChangePasswordRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.LoginRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.events.UserRegisteredEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateEmail;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidPassword;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock AuthUserRepository users;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtEncoder jwtEncoder;
    @Mock RefreshTokenService refreshTokenService;
    @Mock TokenBlacklistService tokenBlacklistService;
    @Mock RCuckooFilter<String> usernameFilter;
    @Mock RCuckooFilter<String> emailFilter;
    @Mock ApplicationEventPublisher events;

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void registrationChecksDuplicatesBeforePersistingAndEmitsRegistrationEvent() {
        var service = service();
        var request = new RegisterRequest("alice", "alice@example.test", "password");
        when(users.existsByUsername("alice")).thenReturn(true);
        assertThrows(DuplicateUsername.class, () -> service.registerUser(request));
        verify(users, never()).save(any());

        when(users.existsByUsername("alice")).thenReturn(false);
        when(users.existsByEmail("alice@example.test")).thenReturn(true);
        assertThrows(DuplicateEmail.class, () -> service.registerUser(request));
        verify(users, never()).save(any());

        when(users.existsByEmail("alice@example.test")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(users.save(any(AuthUser.class)))
                .thenAnswer(
                        invocation -> {
                            AuthUser user = invocation.getArgument(0);
                            ReflectionTestUtils.setField(user, "id", "auth-id");
                            return user;
                        });
        service.registerUser(request);

        verify(usernameFilter).add("alice");
        verify(emailFilter).add("alice@example.test");
        verify(events).publishEvent(new UserRegisteredEvent("auth-id", "alice"));
    }

    @Test
    void loginReturnsAccessAndRefreshTokensAndStoresAuthenticationInContext() {
        var service = service();
        AuthUser user =
                AuthUser.builder()
                        .username("alice")
                        .role(vn.io.huangnosimp.nautilusserver.authentication.constants.Role.USER)
                        .build();
        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtEncoder.encode(any()))
                .thenReturn(
                        Jwt.withTokenValue("access")
                                .header("alg", "HS256")
                                .subject("alice")
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(60))
                                .build());
        when(refreshTokenService.generateRefreshToken(user)).thenReturn("refresh");

        var result = service.loginUser(new LoginRequest("alice", "password"));

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        assertSame(
                authentication,
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication());
    }

    @Test
    void authenticationFailureFromManagerIsPropagated() {
        var service = service();
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));
        assertThrows(
                BadCredentialsException.class,
                () -> service.loginUser(new LoginRequest("alice", "bad")));
    }

    @Test
    void changePasswordRejectsWrongCurrentPasswordWithoutSaving() {
        var service = service();
        AuthUser user = AuthUser.builder().username("alice").password("encoded").build();
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(
                InvalidPassword.class,
                () ->
                        service.changePassword(
                                jwt("alice"), new ChangePasswordRequest("wrong", "new-password")));
        verify(users, never()).save(any());
        verifyNoInteractions(refreshTokenService, tokenBlacklistService);
    }

    @Test
    void changePasswordRejectsAccountsWithoutPassword() {
        var service = service();
        AuthUser user = AuthUser.builder().username("alice").build();
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThrows(
                InvalidPassword.class,
                () ->
                        service.changePassword(
                                jwt("alice"), new ChangePasswordRequest("any", "new-password")));
        verify(users, never()).save(any());
    }

    @Test
    void changePasswordStoresNewHashRevokesSessionsAndIssuesNewTokens() {
        var service = service();
        AuthUser user =
                AuthUser.builder()
                        .username("alice")
                        .password("encoded")
                        .role(vn.io.huangnosimp.nautilusserver.authentication.constants.Role.USER)
                        .build();
        Jwt token = jwt("alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-encoded");
        when(jwtEncoder.encode(any())).thenReturn(jwt("alice"));
        when(refreshTokenService.generateRefreshToken(user)).thenReturn("refresh");

        var result =
                service.changePassword(
                        token, new ChangePasswordRequest("old-password", "new-password"));

        assertEquals("new-encoded", user.getPassword());
        verify(users).save(user);
        verify(tokenBlacklistService).addToken(token);
        verify(refreshTokenService).deleteRefreshToken(user);
        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
    }

    private Jwt jwt(String subject) {
        return Jwt.withTokenValue("access")
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }

    private AuthService service() {
        AuthService service =
                new AuthService(
                        users,
                        passwordEncoder,
                        authenticationManager,
                        jwtEncoder,
                        refreshTokenService,
                        tokenBlacklistService,
                        usernameFilter,
                        emailFilter,
                        events);
        ReflectionTestUtils.setField(service, "jwtExpirationMs", 60_000L);
        return service;
    }
}
