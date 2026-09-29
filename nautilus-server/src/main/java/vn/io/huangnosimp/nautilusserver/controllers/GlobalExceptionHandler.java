package vn.io.huangnosimp.nautilusserver.controllers;

import exceptions.auth.DuplicateEmail;
import exceptions.auth.DuplicateUsername;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateUsername.class)
    public ResponseEntity<String> handleDuplicateUsername(DuplicateUsername e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DuplicateEmail.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmail e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
