package vn.io.huangnosimp.nautilusserver.server.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.GalleryResponse;
import vn.io.huangnosimp.nautilusserver.server.services.GalleryService;

@RestController
@RequestMapping("/servers/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final GalleryService galleryService;

    @GetMapping("/{username}/{servername}")
    public ResponseEntity<List<GalleryResponse>> getGallery(
            @PathVariable String username, @PathVariable String servername) {
        var response = galleryService.getGallery(username, servername);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{username}/{servername}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String username,
            @PathVariable String servername,
            @ModelAttribute UploadImageRequest body) {
        galleryService.uploadImage(username, servername, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/{servername}/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable String imageId) {
        galleryService.deleteImage(username, servername, imageId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/{servername}/{imageId}")
    public ResponseEntity<?> updateImage(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable String imageId,
            UpdateImageInfoRequest body) {
        galleryService.updateImageInfo(username, servername, imageId, body);
        return ResponseEntity.ok().build();
    }
}
