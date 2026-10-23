package vn.io.huangnosimp.nautilusserver.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.http.HttpStatusCode;
import vn.io.huangnosimp.nautilusserver.user.exceptions.CannotUploadAvatar;
import vn.io.huangnosimp.nautilusserver.user.exceptions.UserNotFound;

@RestControllerAdvice
public class UserExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(CannotUploadAvatar.class)
    public ResponseEntity<String> handleCannotUploadAvatar(CannotUploadAvatar e) {
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotFound.class)
    public ResponseEntity<String> handleUserNotFound() {
        return ResponseEntity.notFound().build();
    }
}
