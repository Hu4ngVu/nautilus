package vn.io.huangnosimp.nautilusserver.server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
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
    private final GlobalIdResolver globalIdResolver;

    @GetMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isServerPublic(@globalIdResolver.resolveServerId(#username,"
                + " #servername)) || "
                + "@permissionCheckerImpl.isViewer(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<Page<ImagesResponse>> getGallery(
            @PathVariable String username,
            @PathVariable String servername,
            @PageableDefault(
                            sort = {"orderIndex", "createdAt"},
                            direction = Sort.Direction.ASC)
                    Pageable pageable) {
        var response = galleryService.getAllGallery(getServerId(username, servername), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/{servername}/{imageId}")
    @PreAuthorize(
            "@permissionCheckerImpl.isServerPublic(@globalIdResolver.resolveServerId(#username,"
                + " #servername)) || "
                + "@permissionCheckerImpl.isViewer(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<ImageDetailResponse> getImageDetail(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable String imageId) {
        var response = galleryService.getImageDetail(getServerId(username, servername), imageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> uploadImage(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody UploadImageRequest body) {
        galleryService.uploadImage(getServerId(username, servername), body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}/{servername}/{imageId}")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> deleteImage(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable String imageId) {
        galleryService.deleteImage(getServerId(username, servername), imageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{username}/{servername}/{imageId}")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> updateImage(
            @PathVariable String username,
            @PathVariable String servername,
            @PathVariable String imageId,
            @Valid @RequestBody UpdateImageInfoRequest body) {
        galleryService.updateImageInfo(getServerId(username, servername), imageId, body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private String getServerId(String username, String servername) {
        return globalIdResolver.resolveServerId(username, servername);
    }
}
