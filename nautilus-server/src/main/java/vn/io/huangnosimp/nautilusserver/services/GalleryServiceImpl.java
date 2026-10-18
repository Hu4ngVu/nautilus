package vn.io.huangnosimp.nautilusserver.services;

import io.awspring.cloud.s3.S3Template;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.GalleryResponse;
import vn.io.huangnosimp.nautilusserver.exceptions.CannotUpload;
import vn.io.huangnosimp.nautilusserver.exceptions.ImageNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.models.Gallery;
import vn.io.huangnosimp.nautilusserver.models.Server;
import vn.io.huangnosimp.nautilusserver.repositories.GalleryRepository;
import vn.io.huangnosimp.nautilusserver.repositories.ServerRepository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements  GalleryService {
    private final GalleryRepository galleryRepository;
    private final S3Template s3Template;
    private final ServerRepository serverRepository;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Override
    public List<GalleryResponse> getGallery(String leaderName, String servername) {
        log.debug("Fetching gallery for user: {}, server: {}", leaderName, servername);
        List<GalleryResponse> responseList = galleryRepository.findAllByUsernameServername(leaderName, servername)
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
        log.info("Gallery for user: {}, server: {} fetched successfully", leaderName, servername);
        return responseList;
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void uploadImage(String leaderName, String servername, UploadImageRequest body) {
        log.debug("Uploading image for user: {}, server: {}", leaderName, servername);
        String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
        if (isExtensionInvalid(extension)) {
            throw new IllegalArgumentException("Invalid image extension. Only png, jpg, jpeg, and webp are allowed.");
        }
        Server server = serverRepository.findByUsernameAndServername(leaderName, servername).orElseThrow(() -> new ServerNotFound(servername));
        String key = "gallery/" + server.getUser().getUsername() + "/" + server.getServername() + "-" + UUID.randomUUID() + "." + extension;
        try {
            s3Template.upload(bucketName, key, body.image().getInputStream());
        } catch (Exception e) {
            throw new CannotUpload("Cannot upload image");
        }
        Gallery image = new Gallery();
        setInfo(image, body.title(), body.summary(), body.orderIndex());
        image.setServer(server);
        image.setImageKey(key);
        galleryRepository.save(image);
        log.info("Image uploaded successfully for user: {}, server: {}", leaderName, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(String leaderName, String servername, String imageId) {
        log.debug("Deleting image for user: {}, server: {}, image: {}", leaderName, servername, imageId);
        galleryRepository.findByUsernameServernameId(leaderName, servername, imageId).ifPresent(image ->
            s3Template.deleteObject(bucketName, image.getImageKey())
        );
        galleryRepository.deleteByUsernameServernameId(leaderName, servername, imageId);
        log.info("Image deleted successfully for user: {}, server: {}, image: {}", leaderName, servername, imageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImageInfo(String leaderName, String servername, String imageId, UpdateImageInfoRequest body) {
        log.debug("Updating image info for user: {}, server: {}, image: {}", leaderName, servername, imageId);
        if (allFieldIsNull(body)) {
            return;
        }
        Gallery image = galleryRepository.findByUsernameServernameId(leaderName, servername, imageId).orElseThrow(() -> new ImageNotFound("Image not found"));
        if (body.image() != null) {
            String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid image extension. Only png, jpg, jpeg, and webp are allowed.");
            }
            String key = "gallery/" + image.getServer().getUser().getUsername() + "/" + image.getServer().getServername() + "-" + UUID.randomUUID() + "." + extension;
            try {
                String oldKey = image.getImageKey();
                s3Template.upload(bucketName, key, body.image().getInputStream());
                image.setImageKey(key);
                s3Template.deleteObject(bucketName, oldKey);
            } catch (Exception e) {
                throw new CannotUpload("Cannot upload image");
            }
        }
        setInfo(image, body.title(), body.summary(), body.orderIndex());
        galleryRepository.save(image);
        log.info("Image info updated successfully for user: {}, server: {}, image: {}", leaderName, servername, imageId);
    }

    private void setInfo(Gallery image, String title, String summary, Integer orderIndex) {
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
