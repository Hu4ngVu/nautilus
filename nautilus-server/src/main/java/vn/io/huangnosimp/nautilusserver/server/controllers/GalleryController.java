package vn.io.huangnosimp.nautilusserver.server.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImageDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImagesResponse;
import vn.io.huangnosimp.nautilusserver.server.services.GalleryService;

@RestController
@RequestMapping("/servers/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final GalleryService galleryService;

    @GetMapping("/{username}/{servername}")
    public ResponseEntity<Page<ImagesResponse>> getGallery(
            @PathVariable String username,
            @PathVariable String servername,
            @PageableDefault(
                            sort = {"order_index", "created_at"},
                            direction = Sort.Direction.ASC)
                    Pageable pageable) {
        var response = galleryService.getAllGallery(username, servername, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/{servername}/{imageId}")
    public ResponseEntity<ImageDetailResponse> getImageDetail(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable String imageId) {
        var response = galleryService.getImageDetail(username, servername, imageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{username}/{servername}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String username,
            @PathVariable String servername,
            @ModelAttribute UploadImageRequest body) {
        galleryService.uploadImage(username, servername, body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}/{servername}/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable String imageId) {
        galleryService.deleteImage(username, servername, imageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{username}/{servername}/{imageId}")
    public ResponseEntity<?> updateImage(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable @ModelAttribute String imageId,
            UpdateImageInfoRequest body) {
        galleryService.updateImageInfo(username, servername, imageId, body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
