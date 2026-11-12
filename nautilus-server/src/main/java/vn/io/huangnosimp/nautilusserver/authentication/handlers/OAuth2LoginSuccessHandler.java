package vn.io.huangnosimp.nautilusserver.authentication.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.authentication.services.RefreshTokenService;
import vn.io.huangnosimp.nautilusserver.authentication.utils.RefreshTokenCookies;
import vn.io.huangnosimp.nautilusserver.exceptions.AuthUserNotFound;

/**
 * Completes Google login for the SPA: issues a refresh token cookie and redirects back to the
 * client, which then exchanges the cookie for an access token via {@code POST /auth/refresh}.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthUserRepository authUserRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.client.oauth-redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        AuthUser authUser =
                authUserRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new AuthUserNotFound("User not found: " + email));

        String refreshToken = refreshTokenService.generateRefreshToken(authUser);
        response.addHeader(
                HttpHeaders.SET_COOKIE, RefreshTokenCookies.create(refreshToken).toString());

        // The OAuth2 flow needed a session for the authorization request; it is no longer needed
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(redirectUrl);
    }
}
