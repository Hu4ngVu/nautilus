package vn.io.huangnosimp.nautilusserver.authentication.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.ChangePasswordRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.LoginRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.requests.RegisterRequest;
import vn.io.huangnosimp.nautilusserver.authentication.dto.responses.AccessTokenResponse;
import vn.io.huangnosimp.nautilusserver.authentication.dto.results.TokenResult;
import vn.io.huangnosimp.nautilusserver.authentication.services.AuthService;
import vn.io.huangnosimp.nautilusserver.authentication.utils.RefreshTokenCookies;

@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResult tokenResult = authService.loginUser(loginRequest);

        var cookie = RefreshTokenCookies.create(tokenResult.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokenResult.accessToken()));
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logoutUser(@AuthenticationPrincipal Jwt jwt) {
        authService.logoutUser(jwt);

        var cookie = RefreshTokenCookies.clear();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        TokenResult tokenResult = authService.changePassword(jwt, changePasswordRequest);

        var cookie = RefreshTokenCookies.create(tokenResult.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokenResult.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        TokenResult tokenResult = authService.refreshToken(refreshToken);

        var cookie = RefreshTokenCookies.create(tokenResult.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokenResult.accessToken()));
    }
}
