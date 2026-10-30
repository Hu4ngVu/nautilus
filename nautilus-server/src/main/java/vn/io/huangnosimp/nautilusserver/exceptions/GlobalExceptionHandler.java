package vn.io.huangnosimp.nautilusserver.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Auth
    @ExceptionHandler(AuthUserNotFound.class)
    public ResponseEntity<String> authUserNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DuplicateUsername.class)
    public ResponseEntity<String> handleDuplicateUsername(DuplicateUsername e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DuplicateEmail.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmail e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidRefreshToken.class)
    public ResponseEntity<String> handleInvalidRefreshToken(InvalidRefreshToken e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // User
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<String> handleUserNotFound() {
        return ResponseEntity.notFound().build();
    }

    // Server
    @ExceptionHandler(ImageNotFound.class)
    public ResponseEntity<String> handleImageNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ServerNotFound.class)
    public ResponseEntity<String> handleServerNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UsernameNotFound.class)
    public ResponseEntity<String> handleUsernameNotFound() {
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

    // Storage
    @ExceptionHandler(CannotUpload.class)
    public ResponseEntity<?> handleCannotUpload() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // Star
    @ExceptionHandler(StarNotFound.class)
    public ResponseEntity<String> handleStarNotFound() {
        return ResponseEntity.notFound().build();
    }
}
