package vn.io.huangnosimp.nautilusserver.authentication.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RCuckooFilter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Provider;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.events.UserRegisteredEvent;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {
    @Mock AuthUserRepository users;
    @Mock DefaultOAuth2UserService google;
    @Mock OAuth2UserRequest request;
    @Mock OAuth2User googleUser;
    @Mock ApplicationEventPublisher events;
    @Mock RCuckooFilter<String> usernameFilter;
    @Mock RCuckooFilter<String> emailFilter;

    @Test
    void requiresEmailAndVerifiedEmailFromProvider() {
        var service =
                new CustomOAuth2UserService(users, google, events, usernameFilter, emailFilter);
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
        var service =
                new CustomOAuth2UserService(users, google, events, usernameFilter, emailFilter);
        when(google.loadUser(request)).thenReturn(googleUser);
        when(googleUser.getAttribute("email")).thenReturn("alice@example.test");
        when(googleUser.getAttribute("email_verified")).thenReturn(true);
        AuthUser local =
                AuthUser.builder().email("alice@example.test").provider(Provider.LOCAL).build();
        when(users.findByEmail("alice@example.test")).thenReturn(Optional.of(local));

        assertSame(googleUser, service.loadUser(request));
        assertEquals(Provider.BOTH, local.getProvider());
        verify(users).save(local);
        verify(events, never()).publishEvent(any());

        when(googleUser.getAttribute("email")).thenReturn("new-user@example.test");
        when(users.findByEmail("new-user@example.test")).thenReturn(Optional.empty());
        service.loadUser(request);
        verify(users)
                .save(
                        argThat(
                                user ->
                                        "new_user".equals(user.getUsername())
                                                && "new-user@example.test".equals(user.getEmail())
                                                && user.getProvider() == Provider.GOOGLE
                                                // password is NOT NULL; this one never matches
                                                && user.getPassword() != null
                                                && user.isEnabled()));
        verify(events).publishEvent(any(UserRegisteredEvent.class));
        // Availability checks for registration consult the filters first
        verify(usernameFilter).add("new_user");
        verify(emailFilter).add("new-user@example.test");
    }

    private String usernameCreatedFor(String email) {
        var service =
                new CustomOAuth2UserService(users, google, events, usernameFilter, emailFilter);
        when(google.loadUser(request)).thenReturn(googleUser);
        when(googleUser.getAttribute("email")).thenReturn(email);
        when(googleUser.getAttribute("email_verified")).thenReturn(true);
        when(users.findByEmail(email)).thenReturn(Optional.empty());
        service.loadUser(request);
        var saved = org.mockito.ArgumentCaptor.forClass(AuthUser.class);
        verify(users, atLeastOnce()).save(saved.capture());
        return saved.getValue().getUsername();
    }

    @Test
    void replacesCharactersUsernamesCannotContain() {
        assertEquals("j.doe_tag_x", usernameCreatedFor("j.doe+tag-x@example.test"));
    }

    @Test
    void fitsLocalPartIntoUsernameLength() {
        assertEquals(
                "averyveryverylongnam", usernameCreatedFor("averyveryverylongname@example.test"));
        assertEquals("al_user", usernameCreatedFor("al@example.test"));
    }

    @Test
    void appendsNumberWhenNameIsReservedOrTaken() {
        assertEquals("stars1", usernameCreatedFor("stars@example.test"));
    }

    @Test
    void skipsTakenNumberedNamesAndKeepsMaxLength() {
        when(users.existsByUsername("averyveryverylongnam")).thenReturn(true);
        when(users.existsByUsername("averyveryverylongna1")).thenReturn(true);
        assertEquals(
                "averyveryverylongna2", usernameCreatedFor("averyveryverylongname@example.test"));
    }
}
