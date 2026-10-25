package vn.io.huangnosimp.nautilusserver.auth.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.constants.Provider;
import vn.io.huangnosimp.nautilusserver.auth.constants.Role;
import vn.io.huangnosimp.nautilusserver.auth.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.auth.repositories.AuthUserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final AuthUserRepository authUserRepository;
    private final DefaultOAuth2UserService defaultOAuth2UserService;

    @Override
    @NullMarked
    @Transactional(rollbackFor = Exception.class)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug(
                "Loading OAuth2 user: {}", userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found");
        }
        String username = email.split("@")[0];

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
                            .username(username)
                            .email(email)
                            .provider(Provider.GOOGLE)
                            .role(Role.USER)
                            .build();
            authUserRepository.save(authUser);
        }
        log.info("OAuth2 user loaded: {}", email);
        return oAuth2User;
    }
}
