package vn.io.huangnosimp.nautilusserver.authentication.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RCuckooFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.models.RefreshToken;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.RefreshTokenRepository;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidRefreshToken;

@ExtendWith(MockitoExtension.class)
class AuthenticationServicesTest {
    @Mock AuthUserRepository authUsers;
    @Mock RefreshTokenRepository refreshTokens;
    @Mock RCuckooFilter<String> usernameFilter;
    @Mock RCuckooFilter<String> emailFilter;

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void usernameAndEmailFiltersAvoidDatabaseLookupsForDefiniteMisses() {
        var service = new UsernameEmailCheckingService(usernameFilter, emailFilter, authUsers);
        when(usernameFilter.exists("new-user")).thenReturn(false);
        when(emailFilter.exists("new@example.test")).thenReturn(false);

        assertFalse(service.isUsernameExists("new-user"));
        assertFalse(service.isEmailExists("new@example.test"));
        verifyNoInteractions(authUsers);
    }

    @Test
    void usernameAndEmailFiltersConfirmPossibleMatchesAgainstDatabase() {
        var service = new UsernameEmailCheckingService(usernameFilter, emailFilter, authUsers);
        when(usernameFilter.exists("alice")).thenReturn(true);
        when(emailFilter.exists("alice@example.test")).thenReturn(true);
        when(authUsers.existsByUsername("alice")).thenReturn(true);
        when(authUsers.existsByEmail("alice@example.test")).thenReturn(false);

        assertTrue(service.isUsernameExists("alice"));
        assertFalse(service.isEmailExists("alice@example.test"));
    }

    @Test
    void loadsAuthUserOrRaisesSpringUsernameNotFound() {
        var service = new AuthUserService(authUsers);
        AuthUser alice = AuthUser.builder().username("alice").build();
        when(authUsers.findByUsername("alice")).thenReturn(Optional.of(alice));
        assertSame(alice, service.loadUserByUsername("alice"));
        when(authUsers.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
    }

    @Test
    void loadsAuthUserByEmailWhenNoUsernameMatches() {
        var service = new AuthUserService(authUsers);
        AuthUser alice = AuthUser.builder().username("alice").email("alice@example.test").build();
        when(authUsers.findByUsername("alice@example.test")).thenReturn(Optional.empty());
        when(authUsers.findByEmail("alice@example.test")).thenReturn(Optional.of(alice));
        assertSame(alice, service.loadUserByUsername("alice@example.test"));
    }

    @Test
    void refreshTokenValidationAcceptsUnexpiredTokenAndDeletesExpiredToken() {
        var service = new RefreshTokenService(refreshTokens);
        AuthUser alice = AuthUser.builder().build();
        RefreshToken live =
                RefreshToken.builder()
                        .token("live")
                        .authUser(alice)
                        .expiredAt(Instant.now().plusSeconds(60))
                        .build();
        when(refreshTokens.findByToken("live")).thenReturn(Optional.of(live));
        assertDoesNotThrow(() -> service.validateRefreshToken("live"));
        verify(refreshTokens, never()).delete(live);

        RefreshToken expired =
                RefreshToken.builder()
                        .token("expired")
                        .authUser(alice)
                        .expiredAt(Instant.now().minusSeconds(60))
                        .build();
        when(refreshTokens.findByToken("expired")).thenReturn(Optional.of(expired));
        assertThrows(InvalidRefreshToken.class, () -> service.validateRefreshToken("expired"));
        verify(refreshTokens).delete(expired);
    }

    @Test
    void refreshTokenLookupRejectsUnknownTokenAndReturnsOwner() {
        var service = new RefreshTokenService(refreshTokens);
        AuthUser alice = AuthUser.builder().build();
        ReflectionTestUtils.setField(alice, "id", "user-id");
        when(refreshTokens.findByToken("token"))
                .thenReturn(
                        Optional.of(RefreshToken.builder().authUser(alice).token("token").build()));
        assertEquals("user-id", service.getOwnerId("token"));
        when(refreshTokens.findByToken("missing")).thenReturn(Optional.empty());
        assertThrows(InvalidRefreshToken.class, () -> service.getOwnerId("missing"));
    }

    @Test
    void expiredRefreshTokenIsRejectedWhenRepositoryHasNoRecord() {
        var service = new RefreshTokenService(refreshTokens);
        when(refreshTokens.findByToken("missing")).thenReturn(Optional.empty());
        assertThrows(InvalidRefreshToken.class, () -> service.validateRefreshToken("missing"));
    }

    @Test
    void blacklistValidatorAcceptsLiveTokenAndRejectsBlacklistedToken() {
        TokenBlacklistService blacklist = mock(TokenBlacklistService.class);
        var validator = new JwtBlacklistValidator(blacklist);
        Jwt token = jwt();
        when(blacklist.isBlacklisted(token)).thenReturn(false, true);

        assertEquals(OAuth2TokenValidatorResult.success(), validator.validate(token));
        assertNotNull(validator.validate(token).getErrors().iterator().next());
    }

    @Test
    void blacklistDoesNotStoreAlreadyExpiredTokens() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        var service = new TokenBlacklistService(redis);
        service.addToken(jwt("expired-jwt", Instant.now().minusSeconds(10)));
        verifyNoInteractions(redis);
    }

    @Test
    void blacklistStatusUsesRedisKeyPresence() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.hasKey("jwt-value")).thenReturn(true, false);
        var service = new TokenBlacklistService(redis);
        Jwt token = jwt();
        assertTrue(service.isBlacklisted(token));
        assertFalse(service.isBlacklisted(token));
    }

    private static Jwt jwt() {
        return jwt("jwt-value", Instant.now().plusSeconds(60));
    }

    private static Jwt jwt(String value, Instant expiresAt) {
        return Jwt.withTokenValue(value)
                .header("alg", "none")
                .subject("alice")
                .issuedAt(expiresAt.minusSeconds(120))
                .expiresAt(expiresAt)
                .build();
    }
}
