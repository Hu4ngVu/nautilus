package vn.io.huangnosimp.nautilusserver.server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.huangnosimp.nautilusserver.server.exceptions.*;

@RestControllerAdvice
public class ServerExceptionHandler {
    @ExceptionHandler(ImageNotFound.class)
    public ResponseEntity<String> handleImageNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ServerNotFound.class)
    public ResponseEntity<String> handleServerNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UsernameNotFound.class)
    public ResponseEntity<String> handleUserNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InvalidServerTag.class)
    public ResponseEntity<String> handleInvalidServerTag(InvalidServerTag e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidMcVersion.class)
    public ResponseEntity<String> handleInvalidMcVersion(InvalidMcVersion e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
