package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import vn.io.huangnosimp.nautilusserver.dto.request.LoginUser;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterUser;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;

@RestController
@RequestMapping(value = "/auth", method = RequestMethod.POST)
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @RequestMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUser registerUser) {
        log.info("Registering user: {}", registerUser.username());
        authService.registerUser(registerUser);
        log.info("User {} registered successfully", registerUser.username());
        return ResponseEntity.ok("User registered successfully!");
    }

    @RequestMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUser loginUser) {
        log.info("Logging in user: {}", loginUser.username());
        authService.loginUser(loginUser);
        log.info("User {} logged in successfully", loginUser.username());
        return ResponseEntity.ok("User logged in successfully!");
    }
}
