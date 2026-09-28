package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterUser;
import vn.io.huangnosimp.nautilusserver.mapper.AuthMapper;
import vn.io.huangnosimp.nautilusserver.services.interfaces.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthMapper authMapper;

    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUser registerUser) {
        if (authService.existsByUsername(registerUser.username())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (authService.existsByEmail(registerUser.email())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        authService.registerUser(authMapper.toUser(registerUser));

        return ResponseEntity.ok("User registered successfully!");
    }
}
