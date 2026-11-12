package vn.io.huangnosimp.nautilusserver.authentication.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/** Sends the browser back to the client with an error flag instead of Spring's login page. */
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    @Value("${app.client.oauth-redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {
        response.sendRedirect(
                UriComponentsBuilder.fromUriString(redirectUrl)
                        .queryParam("error", "oauth")
                        .build()
                        .toUriString());
    }
}
