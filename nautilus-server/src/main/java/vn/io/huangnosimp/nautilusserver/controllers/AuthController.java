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
import vn.io.huangnosimp.nautilusserver.dto.request.auth.LoginRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.auth.LoginResponse;
import vn.io.huangnosimp.nautilusserver.dto.result.TokenResult;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;

@RestController
@RequestMapping(value = "/auth", method = RequestMethod.POST)
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registering user: {}", registerRequest.username());
        authService.registerUser(registerRequest);
        log.info("User {} registered successfully", registerRequest.username());
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Logging in user: {}", loginRequest.username());
        TokenResult tokenResult = authService.loginUser(loginRequest);
        LoginResponse accessToken = new LoginResponse(tokenResult.accessToken());

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
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("User logged out successfully!");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        log.info("Refreshing token");
        authService.refreshToken(refreshToken);
        return ResponseEntity.ok("Token refreshed successfully!");
    }
}
