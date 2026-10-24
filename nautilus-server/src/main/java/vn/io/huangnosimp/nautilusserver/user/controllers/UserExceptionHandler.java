package vn.io.huangnosimp.nautilusserver.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.huangnosimp.nautilusserver.user.exceptions.UserNotFound;

@RestControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<String> handleUserNotFound() {
        return ResponseEntity.notFound().build();
    }
}
