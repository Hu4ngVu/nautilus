package vn.io.huangnosimp.nautilusserver.server.services;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.events.GalleryDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.ImageDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.ImageUploadedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.ImageNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.ResourceNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImageDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImagesResponse;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.GalleryRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class GalleryService {
    private final GalleryRepository galleryRepository;
    private final ServerRepository serverRepository;
    private final ResourceStorage resourceStorage;
    private final ApplicationEventPublisher eventPublisher;

    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public Page<ImagesResponse> getAllGallery(String serverId, Pageable pageable) {
        Server server = getServer(serverId);
        return galleryRepository
                .findAllByServer(server, pageable)
                .map(
                        image ->
                                ImagesResponse.builder()
                                        .imageId(image.getId())
                                        .imageTitle(image.getTitle())
                                        .orderIndex(image.getOrderIndex())
                                        .createdAt(image.getCreatedAt())
                                        .imageUrl(resourceStorage.getPublicUrl(image.getImageId()))
                                        .build());
    }

    @Logging(logArguments = true)
    @Transactional(readOnly = true)
    public ImageDetailResponse getImageDetail(String serverId, String imageId) {
        Server server = getServer(serverId);
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
                .imageUrl(resourceStorage.getPublicUrl(image.getImageId()))
                .build();
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void uploadImage(String serverId, UploadImageRequest body) {
        Server server = getServer(serverId);
        Image image =
                Image.builder()
                        .server(server)
                        .title(body.title().orElse(null))
                        .orderIndex(body.orderIndex().orElse(0))
                        .summary(body.summary().orElse(null))
                        .imageId(body.resourceId())
                        .build();
        galleryRepository.save(image);
        eventPublisher.publishEvent(new ImageUploadedEvent(body.resourceId()));
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(String serverId, String imageId) {
        Server server = getServer(serverId);
        Image image =
                galleryRepository
                        .findByServerAndId(server, imageId)
                        .orElseThrow(() -> new ImageNotFound("Image not found"));
        galleryRepository.delete(image);
        eventPublisher.publishEvent(new ImageDeletedEvent(image.getImageId()));
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateImageInfo(String serverId, String imageId, UpdateImageInfoRequest body) {
        Server server = getServer(serverId);
        Image image =
                galleryRepository
                        .findByServerAndId(server, imageId)
                        .orElseThrow(() -> new ImageNotFound("Image not found"));

        if (body.resourceId() != null) {
            if (!resourceStorage.isResourceIdExist(body.resourceId())) {
                throw new ResourceNotFound("Resource not found");
            }
            if (image.getImageId() != null) {
                eventPublisher.publishEvent(new ImageDeletedEvent(image.getImageId()));
            }
            eventPublisher.publishEvent(new ImageUploadedEvent(body.resourceId()));
            image.setImageId(body.resourceId());
        }
        body.orderIndex().ifPresent(image::setOrderIndex);
        body.title().ifPresent(image::setTitle);
        body.summary().ifPresent(image::setSummary);
        galleryRepository.save(image);
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteGallery(String serverId) {
        Server server = getServer(serverId);
        var imageIds =
                galleryRepository.findAllByServer(server).stream().map(Image::getImageId).toList();
        if (!imageIds.isEmpty()) {
            eventPublisher.publishEvent(new GalleryDeletedEvent(imageIds));
        }
    }

    private @NonNull Server getServer(String serverId) {
        return serverRepository
                .findById(serverId)
                .orElseThrow(() -> new ServerNotFound("Server not found"));
    }
}
