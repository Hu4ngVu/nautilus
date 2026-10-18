package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.dto.request.LoginRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.AccessTokenResponse;
import vn.io.huangnosimp.nautilusserver.dto.result.TokenResult;
import vn.io.huangnosimp.nautilusserver.services.auth.AuthService;

@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registering user: {}", registerRequest.username());
        authService.registerUser(registerRequest);
        log.info("User {} registered successfully", registerRequest.username());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Logging in user: {}", loginRequest.username());
        TokenResult tokenResult = authService.loginUser(loginRequest);
        AccessTokenResponse accessToken = new AccessTokenResponse(tokenResult.accessToken());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResult.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 24 * 60 * 60) // 7 days
                .sameSite(Cookie.SameSite.LAX.name())
                .build();
        log.info("User {} logged in successfully", loginRequest.username());
        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(accessToken);
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logoutUser(@AuthenticationPrincipal Jwt jwt) {
        log.info("Logging out user");

        authService.logoutUser(jwt);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite(Cookie.SameSite.LAX.name())
                .build();
        log.info("User logged out successfully");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        log.info("Refreshing token");
        TokenResult tokenResult = authService.refreshToken(refreshToken);
        AccessTokenResponse accessToken = new AccessTokenResponse(tokenResult.accessToken());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResult.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 24 * 60 * 60) // 7 days
                .sameSite(Cookie.SameSite.LAX.name())
                .build();
        log.info("Token refreshed successfully");
        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(accessToken);
    }
}
