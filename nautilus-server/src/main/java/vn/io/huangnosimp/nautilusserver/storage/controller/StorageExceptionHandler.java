package vn.io.huangnosimp.nautilusserver.storage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.huangnosimp.nautilusserver.storage.exceptions.CannotUpload;

@RestControllerAdvice
public class StorageExceptionHandler {
    @ExceptionHandler(CannotUpload.class)
    public ResponseEntity<?> handleCannotUpload() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
