package vn.io.huangnosimp.nautilusserver.authentication.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.redisson.api.RCuckooFilter;
import org.slf4j.event.Level;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Provider;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Role;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.constraints.NotReservedUsernameValidator;
import vn.io.huangnosimp.nautilusserver.events.UserRegisteredEvent;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    // Mirrors the username constraints on RegisterRequest
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    // The password column is NOT NULL; this is not a BCrypt hash, so no password ever matches it
    private static final String NO_PASSWORD = "!";

    private final AuthUserRepository authUserRepository;
    private final DefaultOAuth2UserService defaultOAuth2UserService;
    private final ApplicationEventPublisher eventPublisher;
    private final RCuckooFilter<String> usernameCuckooFilter;
    private final RCuckooFilter<String> emailCuckooFilter;

    @Override
    @NullMarked
    @Logging(level = Level.DEBUG)
    @Transactional(rollbackFor = Exception.class)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found");
        }
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        if (emailVerified == null || !emailVerified) {
            throw new OAuth2AuthenticationException("Email not verified");
        }

        Optional<AuthUser> userOptional = authUserRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            AuthUser authUser = userOptional.get();
            if (authUser.getProvider().equals(Provider.LOCAL)) {
                authUser.setProvider(Provider.BOTH);
                authUserRepository.save(authUser);
            }
        } else {
            AuthUser authUser =
                    AuthUser.builder()
                            .username(usernameFor(email))
                            .email(email)
                            .password(NO_PASSWORD)
                            .provider(Provider.GOOGLE)
                            .role(Role.USER)
                            .enabled(true)
                            .build();
            authUserRepository.save(authUser);
            usernameCuckooFilter.add(authUser.getUsername());
            emailCuckooFilter.add(authUser.getEmail());
            eventPublisher.publishEvent(
                    new UserRegisteredEvent(authUser.getId(), authUser.getUsername()));
        }
        return oAuth2User;
    }

    /** Builds a valid, free username from the email's local part, numbering it when needed. */
    private String usernameFor(String email) {
        String base = email.substring(0, email.lastIndexOf('@')).replaceAll("[^a-zA-Z0-9_.]", "_");
        if (base.length() < MIN_USERNAME_LENGTH) {
            base += "_user";
        }
        base = base.substring(0, Math.min(base.length(), MAX_USERNAME_LENGTH));
        if (isAvailable(base)) {
            return base;
        }
        for (int n = 1; ; n++) {
            String suffix = String.valueOf(n);
            String candidate =
                    base.substring(
                                    0,
                                    Math.min(base.length(), MAX_USERNAME_LENGTH - suffix.length()))
                            + suffix;
            if (isAvailable(candidate)) {
                return candidate;
            }
        }
    }

    private boolean isAvailable(String username) {
        return !NotReservedUsernameValidator.isReserved(username)
                && !authUserRepository.existsByUsername(username);
    }
}
