package vn.io.huangnosimp.nautilusserver.controllers;

import software.amazon.awssdk.http.HttpStatusCode;
import vn.io.huangnosimp.nautilusserver.exceptions.*;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.DuplicateEmail;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.DuplicateUsername;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.InvalidRefreshToken;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;

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

    @ExceptionHandler(InvalidRefreshToken.class)
    public ResponseEntity<String> handleInvalidRefreshToken(InvalidRefreshToken e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<String> handleUserNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ServerNotFound.class)
    public ResponseEntity<String> handleServerNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(CannotUpload.class)
    public ResponseEntity<String> handleCannotUpload(CannotUpload e) {
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(InvitationNotFound.class)
    public ResponseEntity<String> handleInvitationNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DuplicateInvitation.class)
    public ResponseEntity<String> handleDuplicateInvitation(DuplicateInvitation e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DuplicateTransfer.class)
    public ResponseEntity<String> handleDuplicateTransfer(DuplicateTransfer e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidTransferStatus.class)
    public ResponseEntity<String> handleInvalidTransferStatus(InvalidTransferStatus e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(TransferNotFound.class)
    public ResponseEntity<String> handleTransferNotFound() {
        return ResponseEntity.notFound().build();
    }
}
