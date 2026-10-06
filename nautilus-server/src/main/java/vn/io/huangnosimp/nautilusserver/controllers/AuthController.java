package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.LoginUser;
import vn.io.huangnosimp.nautilusserver.dto.request.auth.RegisterUser;
import vn.io.huangnosimp.nautilusserver.dto.response.auth.Login;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;

@RestController
@RequestMapping(value = "/auth", method = RequestMethod.POST)
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUser registerUser) {
        log.info("Registering user: {}", registerUser.username());
        authService.registerUser(registerUser);
        log.info("User {} registered successfully", registerUser.username());
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUser loginUser) {
        log.info("Logging in user: {}", loginUser.username());
        Login accessToken = authService.loginUser(loginUser);
        log.info("User {} logged in successfully", loginUser.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(accessToken);
    }
}
