package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.List;
import java.util.UUID;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.server.DeleteImageEvent;
import vn.io.huangnosimp.nautilusserver.server.DeleteImagesBatchEvent;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImageDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImagesResponse;
import vn.io.huangnosimp.nautilusserver.server.exceptions.ImageNotFound;
import vn.io.huangnosimp.nautilusserver.server.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.server.exceptions.UsernameNotFound;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.GalleryRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.storage.StorageService;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {
    private final GalleryRepository galleryRepository;
    private final AuthUserUtils authUserUtils;
    private final ServerRepository serverRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

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
                                                        storageService.createSignedGetUrl(
                                                                image.getImageKey()))
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
                .imageUrl(storageService.createSignedGetUrl(image.getImageKey()))
                .build();
    }

    @Override
    public void uploadImage(String username, String servername, UploadImageRequest body) {
        log.debug("Uploading image for user: {}, server: {}", username, servername);
        Server server = getServer(username, servername);

        String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
        if (isExtensionInvalid(extension)) {
            throw new IllegalArgumentException(
                    "Invalid image extension. Only png, jpg, jpeg, and webp are allowed.");
        }

        String key =
                "gallery/"
                        + username
                        + "/"
                        + server.getServername()
                        + "-"
                        + UUID.randomUUID()
                        + "."
                        + extension;
        storageService.uploadImage(body.image(), key);
        try {
            Image image = new Image();
            setInfo(image, body.title(), body.summary(), body.orderIndex());
            image.setServer(server);
            image.setImageKey(key);
            galleryRepository.save(image);
        } catch (Exception e) {
            eventPublisher.publishEvent(new DeleteImageEvent(key));
            throw e;
        }
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
        galleryRepository.delete(image);
        eventPublisher.publishEvent(new DeleteImageEvent(image.getImageKey()));
        log.info(
                "Image deleted successfully for user: {}, server: {}, image: {}",
                username,
                servername,
                imageId);
    }

    @Override
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

        String oldKey = image.getImageKey();
        String newKey = null;
        boolean isImageUploaded = false;

        MultipartFile imageFile = body.image();
        if (imageFile != null) {
            String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException(
                        "Invalid image extension. Only png, jpg, jpeg, and webp are allowed.");
            }
            newKey =
                    "gallery/"
                            + username
                            + "/"
                            + image.getServer().getServername()
                            + "-"
                            + UUID.randomUUID()
                            + "."
                            + extension;
            storageService.uploadImage(imageFile, newKey);
            isImageUploaded = true;
        }

        try {
            setInfo(image, body.title(), body.summary(), body.orderIndex());
            galleryRepository.save(image);
            log.info(
                    "Image info updated successfully for user: {}, server: {}, image: {}",
                    username,
                    servername,
                    imageId);
        } catch (Exception e) {
            if (isImageUploaded) {
                eventPublisher.publishEvent(new DeleteImageEvent(newKey));
            }
            throw e;
        }

        if (isImageUploaded && oldKey != null) {
            eventPublisher.publishEvent(new DeleteImageEvent(oldKey));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGallery(String userId, String servername) {
        log.debug("Deleting gallery for user: {}, server: {}", userId, servername);
        List<String> keys =
                galleryRepository.findAllImageKeysByUserIdAndServername(userId, servername);
        if (keys.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(new DeleteImagesBatchEvent(keys));
        log.info("Gallery deleted successfully for user: {}, server: {}", userId, servername);
    }

    private @NonNull Server getServer(String username, String servername) {
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UsernameNotFound(username));
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

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || extension.isBlank()
                || !extension.equalsIgnoreCase("png")
                        && !extension.equalsIgnoreCase("jpg")
                        && !extension.equalsIgnoreCase("jpeg")
                        && !extension.equalsIgnoreCase("webp");
    }

    private boolean allFieldIsNull(@NonNull UpdateImageInfoRequest body) {
        return body.image() == null
                && body.title() == null
                && body.summary() == null
                && body.orderIndex() == null;
    }
}
