package vn.io.huangnosimp.nautilusserver.server.services;

import io.awspring.cloud.s3.S3Template;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.GalleryResponse;
import vn.io.huangnosimp.nautilusserver.server.exceptions.CannotUploadImage;
import vn.io.huangnosimp.nautilusserver.server.exceptions.ImageNotFound;
import vn.io.huangnosimp.nautilusserver.server.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.server.exceptions.UsernameNotFound;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.GalleryRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {
    private final GalleryRepository galleryRepository;
    private final S3Template s3Template;
    private final AuthUserUtils authUserUtils;
    private final ServerRepository serverRepository;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Override
    public List<GalleryResponse> getGallery(String username, String servername) {
        log.debug("Fetching gallery for user: {}, server: {}", username, servername);
        String userId = getUserId(username);
        List<GalleryResponse> responses = galleryRepository.findAllByUserIdServername(userId, servername)
                .stream()
                .map(gallery -> GalleryResponse.builder()
                        .imageId(gallery.getId())
                        .imageUrl(s3Template.createSignedGetURL(bucketName, gallery.getImageKey(), Duration.ofMinutes(1)).toString())
                        .imageTitle(gallery.getTitle())
                        .imageSummary(gallery.getSummary())
                        .orderIndex(gallery.getOrderIndex())
                        .createdAt(gallery.getCreatedAt())
                        .build())
                .toList();
        log.info("Gallery for user: {}, server: {} fetched successfully", username, servername);
        return responses;
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void uploadImage(String username, String servername, UploadImageRequest body) {
        log.debug("Uploading image for user: {}, server: {}", username, servername);
        String userId = getUserId(username);
        String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
        if (isExtensionInvalid(extension)) {
            throw new IllegalArgumentException("Invalid image extension. Only png, jpg, jpeg, and webp are allowed.");
        }
        Server server = serverRepository.findByUserIdAndServername(userId, servername).orElseThrow(() -> new ServerNotFound(servername));
        String key = "gallery/" + username + "/" + server.getServername() + "-" + UUID.randomUUID() + "." + extension;
        try {
            s3Template.upload(bucketName, key, body.image().getInputStream());
        } catch (Exception e) {
            throw new CannotUploadImage("Cannot upload image");
        }
        Image image = new Image();
        setInfo(image, body.title(), body.summary(), body.orderIndex());
        image.setServer(server);
        image.setImageKey(key);
        galleryRepository.save(image);
        log.info("Image uploaded successfully for user: {}, server: {}", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(String username, String servername, String imageId) {
        log.debug("Deleting image for user: {}, server: {}, image: {}", username, servername, imageId);
        String userId = getUserId(username);
        galleryRepository.findByUserIdServernameId(userId, servername, imageId).ifPresent(image ->
            s3Template.deleteObject(bucketName, image.getImageKey())
        );
        galleryRepository.deleteByUserIdServernameId(userId, servername, imageId);
        log.info("Image deleted successfully for user: {}, server: {}, image: {}", username, servername, imageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImageInfo(String username, String servername, String imageId, UpdateImageInfoRequest body) {
        log.debug("Updating image info for user: {}, server: {}, image: {}", username, servername, imageId);
        if (allFieldIsNull(body)) {
            return;
        }
        String userId = getUserId(username);
        Image image = galleryRepository.findByUserIdServernameId(userId, servername, imageId).orElseThrow(() -> new ImageNotFound("Image not found"));
        if (body.image() != null) {
            String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid image extension. Only png, jpg, jpeg, and webp are allowed.");
            }
            String key = "gallery/" + username + "/" + image.getServer().getServername() + "-" + UUID.randomUUID() + "." + extension;
            try {
                String oldKey = image.getImageKey();
                s3Template.upload(bucketName, key, body.image().getInputStream());
                image.setImageKey(key);
                s3Template.deleteObject(bucketName, oldKey);
            } catch (Exception e) {
                throw new CannotUploadImage("Cannot upload image");
            }
        }
        setInfo(image, body.title(), body.summary(), body.orderIndex());
        galleryRepository.save(image);
        log.info("Image info updated successfully for user: {}, server: {}, image: {}", username, servername, imageId);
    }

    @Override
    public void deleteGallery(String userId, String servername) {
        log.debug("Deleting gallery for user: {}, server: {}", userId, servername);
        List<Image> images = galleryRepository.findAllByUserIdServername(userId, servername);
        for (Image image : images) {
            s3Template.deleteObject(bucketName, image.getImageKey());
        }
        galleryRepository.deleteAll(images);
        log.info("Gallery deleted successfully for user: {}, server: {}", userId, servername);
    }

    private @NonNull String getUserId(String username) {
        return authUserUtils.getAuthUserIdByUsername(username).orElseThrow(() -> new UsernameNotFound(username));
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
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || !extension.equalsIgnoreCase("png")
                && !extension.equalsIgnoreCase("jpg")
                && !extension.equalsIgnoreCase("jpeg")
                && !extension.equalsIgnoreCase("webp");
    }

    private boolean allFieldIsNull(UpdateImageInfoRequest body) {
        return body.image() == null
                && body.title() == null
                && body.summary() == null
                && body.orderIndex() == null;
    }
}
