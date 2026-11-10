package vn.io.huangnosimp.nautilusserver.authentication.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Provider;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {
    @Mock AuthUserRepository users;
    @Mock DefaultOAuth2UserService google;
    @Mock OAuth2UserRequest request;
    @Mock OAuth2User googleUser;

    @Test
    void requiresEmailAndVerifiedEmailFromProvider() {
        var service = new CustomOAuth2UserService(users, google);
        when(google.loadUser(request)).thenReturn(googleUser);
        when(googleUser.getAttribute("email")).thenReturn(null);
        assertThrows(OAuth2AuthenticationException.class, () -> service.loadUser(request));

        when(googleUser.getAttribute("email")).thenReturn("alice@example.test");
        when(googleUser.getAttribute("email_verified")).thenReturn(false);
        assertThrows(OAuth2AuthenticationException.class, () -> service.loadUser(request));
        verifyNoInteractions(users);
    }

    @Test
    void linksExistingLocalAccountAndCreatesGoogleAccountWhenEmailIsNew() {
        var service = new CustomOAuth2UserService(users, google);
        when(google.loadUser(request)).thenReturn(googleUser);
        when(googleUser.getAttribute("email")).thenReturn("alice@example.test");
        when(googleUser.getAttribute("email_verified")).thenReturn(true);
        AuthUser local =
                AuthUser.builder().email("alice@example.test").provider(Provider.LOCAL).build();
        when(users.findByEmail("alice@example.test")).thenReturn(Optional.of(local));

        assertSame(googleUser, service.loadUser(request));
        assertEquals(Provider.BOTH, local.getProvider());
        verify(users).save(local);

        when(googleUser.getAttribute("email")).thenReturn("new-user@example.test");
        when(users.findByEmail("new-user@example.test")).thenReturn(Optional.empty());
        service.loadUser(request);
        verify(users)
                .save(
                        argThat(
                                user ->
                                        "new-user".equals(user.getUsername())
                                                && "new-user@example.test".equals(user.getEmail())
                                                && user.getProvider() == Provider.GOOGLE));
    }
}
