package vn.io.huangnosimp.nautilusserver.services.auth;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.constants.Provider;
import vn.io.huangnosimp.nautilusserver.constants.Role;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    @NullMarked
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found");
        }
        if (email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found");
        }
        String username = email.split("@")[0];

        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        if (emailVerified == null || !emailVerified) {
            throw new OAuth2AuthenticationException("Email not verified");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProvider().equals(Provider.LOCAL)) {
                user.setProvider(Provider.BOTH);
                userRepository.save(user);
            }
        }
        else {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .provider(Provider.GOOGLE)
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
        }
        return oAuth2User;
    }
}
