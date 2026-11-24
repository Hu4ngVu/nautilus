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

    @ExceptionHandler(InvalidPassword.class)
    public ResponseEntity<String> handleInvalidPassword(InvalidPassword e) {
        return ResponseEntity.badRequest().body(e.getMessage());
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

    @ExceptionHandler(UserNotFound.class)
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

    @ExceptionHandler(DuplicateServer.class)
    public ResponseEntity<String> handleDuplicateServer(DuplicateServer e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Storage
    @ExceptionHandler(CannotUpload.class)
    public ResponseEntity<?> handleCannotUpload() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(InvalidContentType.class)
    public ResponseEntity<String> handleInvalidContentType(InvalidContentType e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidResourceId.class)
    public ResponseEntity<String> handleInvalidResourceId(InvalidResourceId e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<String> handleResourceNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InvalidWebhookSecretKey.class)
    public ResponseEntity<String> handleInvalidWebhookSecretKey(InvalidWebhookSecretKey e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    // Star
    @ExceptionHandler(StarNotFound.class)
    public ResponseEntity<String> handleStarNotFound() {
        return ResponseEntity.notFound().build();
    }
}
