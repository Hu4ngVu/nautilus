package vn.io.huangnosimp.nautilusserver.authentication.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.authentication.services.RefreshTokenService;
import vn.io.huangnosimp.nautilusserver.exceptions.AuthUserNotFound;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginHandlersTest {
    @Mock AuthUserRepository users;
    @Mock RefreshTokenService refreshTokenService;

    @Test
    void successSetsRefreshCookieAndRedirectsToClient() throws Exception {
        var handler = new OAuth2LoginSuccessHandler(users, refreshTokenService);
        ReflectionTestUtils.setField(handler, "redirectUrl", "/auth/callback");
        AuthUser user = AuthUser.builder().username("alice").email("alice@example.test").build();
        when(users.findByEmail("alice@example.test")).thenReturn(Optional.of(user));
        when(refreshTokenService.generateRefreshToken(user)).thenReturn("refresh");
        var request = new MockHttpServletRequest();
        request.getSession(true);
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authenticationFor("alice@example.test"));

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertTrue(cookie.startsWith("refreshToken=refresh"));
        assertTrue(cookie.contains("HttpOnly"));
        assertEquals("/auth/callback", response.getRedirectedUrl());
        assertNull(request.getSession(false));
    }

    @Test
    void successFailsWhenUserWasNotPersisted() {
        var handler = new OAuth2LoginSuccessHandler(users, refreshTokenService);
        ReflectionTestUtils.setField(handler, "redirectUrl", "/auth/callback");
        when(users.findByEmail("ghost@example.test")).thenReturn(Optional.empty());

        assertThrows(
                AuthUserNotFound.class,
                () ->
                        handler.onAuthenticationSuccess(
                                new MockHttpServletRequest(),
                                new MockHttpServletResponse(),
                                authenticationFor("ghost@example.test")));
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void failureRedirectsToClientWithErrorFlag() throws Exception {
        var handler = new OAuth2LoginFailureHandler();
        ReflectionTestUtils.setField(handler, "redirectUrl", "/auth/callback");
        var response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                new MockHttpServletRequest(),
                response,
                new OAuth2AuthenticationException("Email not verified"));

        assertEquals("/auth/callback?error=oauth", response.getRedirectedUrl());
    }

    private static TestingAuthenticationToken authenticationFor(String email) {
        var principal =
                new DefaultOAuth2User(List.of(), Map.of("sub", "google-id", "email", email), "sub");
        return new TestingAuthenticationToken(principal, null);
    }
}
