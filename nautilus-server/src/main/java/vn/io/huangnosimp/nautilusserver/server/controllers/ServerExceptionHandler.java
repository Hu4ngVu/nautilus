package vn.io.huangnosimp.nautilusserver.server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.http.HttpStatusCode;
import vn.io.huangnosimp.nautilusserver.server.exceptions.*;

@RestControllerAdvice
public class ServerExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(CannotUploadImage.class)
    public ResponseEntity<String> handleCannotUploadImage(CannotUploadImage e) {
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CannotUploadServerAvatar.class)
    public ResponseEntity<String> handleCannotUploadServerAvatar(CannotUploadServerAvatar e) {
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CannotUploadServerBanner.class)
    public ResponseEntity<String> handleCannotUploadServerBanner(CannotUploadServerBanner e) {
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ImageNotFound.class)
    public ResponseEntity<String> handleImageNotFound() {
        return ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ServerNotFound.class)
    public ResponseEntity<String> handleServerNotFound() {
        return ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UsernameNotFound.class)
    public ResponseEntity<String> handleUserNotFound() {
        return ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidServerTag.class)
    public ResponseEntity<String> handleInvalidServerTag(InvalidServerTag e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
