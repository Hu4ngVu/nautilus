package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterUser;
import vn.io.huangnosimp.nautilusserver.mapper.AuthMapper;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;

@RestController
@RequestMapping(value = "/auth", method = RequestMethod.POST)
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final AuthMapper authMapper;

    @RequestMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUser registerUser) {
        log.info("Registering user: {}", registerUser.username());
        if (authService.existsByUsername(registerUser.username())) {
            log.info("Username {} is already taken", registerUser.username());
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (authService.existsByEmail(registerUser.email())) {
            log.info("Email {} is already in use", registerUser.email());
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        authService.registerUser(authMapper.toUser(registerUser));
        log.info("User {} registered successfully", registerUser.username());

        return ResponseEntity.ok("User registered successfully!");
    }
}
