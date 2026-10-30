package vn.io.huangnosimp.nautilusserver.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.ImageNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImageDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImagesResponse;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.GalleryRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {
    private final GalleryRepository galleryRepository;
    private final AuthUserUtils authUserUtils;
    private final ServerRepository serverRepository;
    private final StorageUtils storageUtils;

    @Override
    @Transactional(readOnly = true)
    public Page<ImagesResponse> getAllGallery(
            String username, String servername, Pageable pageable) {
        log.debug("Fetching gallery for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);
        Page<ImagesResponse> responses =
                galleryRepository
                        .findAllByServer(server, pageable)
                        .map(
                                image ->
                                        ImagesResponse.builder()
                                                .imageId(image.getId())
                                                .imageTitle(image.getTitle())
                                                .orderIndex(image.getOrderIndex())
                                                .createdAt(image.getCreatedAt())
                                                .imageUrl(
                                                        storageUtils.createSignedGetUrl(
                                                                image.getId(),
                                                                StorageType.GALLERY_IMAGE))
                                                .build());

        log.info("Gallery for user: {}, server: {} fetched successfully", username, servername);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public ImageDetailResponse getImageDetail(String username, String servername, String imageId) {
        Server server = getServer(username, servername);
        Image image =
                galleryRepository
                        .findByServerAndId(server, imageId)
                        .orElseThrow(() -> new ImageNotFound("Image not found"));
        return ImageDetailResponse.builder()
                .imageId(image.getId())
                .imageTitle(image.getTitle())
                .summary(image.getSummary())
                .orderIndex(image.getOrderIndex())
                .createdAt(image.getCreatedAt())
                .imageUrl(storageUtils.createSignedGetUrl(image.getId(), StorageType.GALLERY_IMAGE))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadImage(String username, String servername, UploadImageRequest body) {
        log.debug("Uploading image for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);
        Image image =
                Image.builder()
                        .server(server)
                        .title(body.title())
                        .orderIndex(body.orderIndex())
                        .summary(body.summary())
                        .build();
        storageUtils.setResourceActive(
                body.resourceId(), image.getId(), StorageType.GALLERY_IMAGE, server.getUserId());
        galleryRepository.save(image);
        log.info("Image uploaded successfully for user: {}, server: {}", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(String username, String servername, String imageId) {
        log.debug(
                "Deleting image for user: {}, server: {}, image: {}",
                username,
                servername,
                imageId);
        Server server = getServer(username, servername);
        Image image =
                galleryRepository
                        .findByServerAndId(server, imageId)
                        .orElseThrow(() -> new ImageNotFound("Image not found"));
        storageUtils.setResourceDeleted(
                image.getId(), StorageType.GALLERY_IMAGE, server.getUserId());
        galleryRepository.delete(image);
        log.info(
                "Image deleted successfully for user: {}, server: {}, image: {}",
                username,
                servername,
                imageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImageInfo(
            String username, String servername, String imageId, UpdateImageInfoRequest body) {
        log.debug(
                "Updating image info for user: {}, server: {}, image: {}",
                username,
                servername,
                imageId);
        if (allFieldIsNull(body)) {
            return;
        }
        Server server = getServer(username, servername);
        Image image =
                galleryRepository
                        .findByServerAndId(server, imageId)
                        .orElseThrow(() -> new ImageNotFound("Image not found"));

        if (body.resourceId() != null) {
            storageUtils.setResourceDeleted(
                    image.getId(), StorageType.GALLERY_IMAGE, server.getUserId());
            storageUtils.setResourceActive(
                    body.resourceId(),
                    image.getId(),
                    StorageType.GALLERY_IMAGE,
                    server.getUserId());
        }
        setInfo(image, body.title(), body.summary(), body.orderIndex());
        galleryRepository.save(image);
        log.info(
                "Image info updated successfully for user: {}, server: {}, image: {}",
                username,
                servername,
                imageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGallery(String userId, String servername) {
        log.debug("Deleting gallery for user: {}, server: {}", userId, servername);
        var imageIds = galleryRepository.findAllImageIdsByUserIdAndServername(userId, servername);
        if (!imageIds.isEmpty()) {
            storageUtils.setResourceDeletedBatch(imageIds, StorageType.GALLERY_IMAGE, userId);
        }
        log.info("Gallery deleted successfully for user: {}, server: {}", userId, servername);
    }

    private @NonNull Server getServer(String username, String servername) {
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        return serverRepository
                .findByUserIdAndServername(userId, servername)
                .orElseThrow(() -> new ServerNotFound(servername));
    }

    private void setInfo(Image image, String title, String summary, Integer orderIndex) {
        if (title != null) {
            image.setTitle(title);
        }
        if (summary != null) {
            image.setSummary(summary);
        }
        if (orderIndex != null) {
            image.setOrderIndex(orderIndex);
        }
        if (orderIndex == null) {
            image.setOrderIndex(0);
        }
    }

    private boolean allFieldIsNull(@NonNull UpdateImageInfoRequest body) {
        return body.resourceId() == null
                && body.title() == null
                && body.summary() == null
                && body.orderIndex() == null;
    }
}
