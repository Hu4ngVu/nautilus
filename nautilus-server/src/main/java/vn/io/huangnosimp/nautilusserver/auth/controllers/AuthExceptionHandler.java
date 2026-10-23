package vn.io.huangnosimp.nautilusserver.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.huangnosimp.nautilusserver.auth.exceptions.AuthUserNotFound;
import vn.io.huangnosimp.nautilusserver.auth.exceptions.DuplicateEmail;
import vn.io.huangnosimp.nautilusserver.auth.exceptions.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.auth.exceptions.InvalidRefreshToken;

@RestControllerAdvice
public class AuthExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(AuthUserNotFound.class)
    public ResponseEntity<String> authUserNotFound() {
        return ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DuplicateUsername.class)
    public ResponseEntity<String> handleDuplicateUsername(DuplicateUsername e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DuplicateEmail.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmail e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidRefreshToken.class)
    public ResponseEntity<String> handleInvalidRefreshToken(InvalidRefreshToken e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
