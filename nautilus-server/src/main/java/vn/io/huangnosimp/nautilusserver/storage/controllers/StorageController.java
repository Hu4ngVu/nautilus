package vn.io.huangnosimp.nautilusserver.storage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.storage.dto.requests.UploadCompleteRequest;
import vn.io.huangnosimp.nautilusserver.storage.dto.responses.UploadUrlResponse;
import vn.io.huangnosimp.nautilusserver.storage.services.StorageService;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @GetMapping("/upload-url")
    public ResponseEntity<UploadUrlResponse> getUploadUrl(
            @RequestParam String contentType, @RequestParam Long contentLength) {
        return ResponseEntity.ok(storageService.generateUploadUrl(contentType, contentLength));
    }

    @PutMapping("webhook/upload-complete")
    public ResponseEntity<?> handleUploadCompleteWebhook(
            @RequestHeader("X-Secret-Key") String secretKey,
            @RequestBody UploadCompleteRequest request) {
        storageService.completeUpload(secretKey, request);
        return ResponseEntity.ok().build();
    }
}
