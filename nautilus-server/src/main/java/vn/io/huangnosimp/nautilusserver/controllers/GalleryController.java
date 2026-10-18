package vn.io.huangnosimp.nautilusserver.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.GalleryResponse;
import vn.io.huangnosimp.nautilusserver.services.GalleryService;

import java.util.List;

@RestController
@RequestMapping("/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final GalleryService galleryService;

    @GetMapping("/{leaderName}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isPublic(#leaderName, #servername) or @serverAccessEvaluator.isOwner(authentication.name, #leaderName, #servername)")
    public ResponseEntity<List<GalleryResponse>> getGallery(@PathVariable String leaderName, @PathVariable String servername) {
        var response = galleryService.getGallery(leaderName, servername);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{leaderName}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isOwner(authentication.name, #leaderName, #servername)")
    public ResponseEntity<?> uploadGallery(@PathVariable String leaderName, @PathVariable String servername, @ModelAttribute UploadImageRequest body) {
        galleryService.uploadImage(leaderName, servername, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{leaderName}/{servername}/{imageId}")
    @PreAuthorize("@serverAccessEvaluator.isOwner(authentication.name, #leaderName, #servername)")
    public ResponseEntity<?> deleteGallery(@PathVariable String leaderName, @PathVariable String servername, @PathVariable String imageId) {
        galleryService.deleteImage(leaderName, servername, imageId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{leaderName}/{servername}/{imageId}")
    @PreAuthorize("@serverAccessEvaluator.isOwner(authentication.name, #leaderName, #servername)")
    public ResponseEntity<?> updateGallery(@PathVariable String leaderName, @PathVariable String servername, @PathVariable String imageId, UpdateImageInfoRequest body) {
        galleryService.updateImageInfo(leaderName, servername, imageId, body);
        return ResponseEntity.ok().build();
    }
}
