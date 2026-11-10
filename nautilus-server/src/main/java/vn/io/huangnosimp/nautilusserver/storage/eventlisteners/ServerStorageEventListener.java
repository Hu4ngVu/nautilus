package vn.io.huangnosimp.nautilusserver.storage.eventlisteners;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.events.*;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@Service
@RequiredArgsConstructor
public class ServerStorageEventListener {
    private final ResourceRepository resourceRepository;

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(ServerDeletedEvent event) {
        resourceRepository.setDeletedById(event.serverAvatarId());
        resourceRepository.setDeletedById(event.serverBannerId());
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(ServerAvatarDeletedEvent event) {
        resourceRepository.setDeletedById(event.resourceId());
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(ServerAvatarUpdatedEvent event) {
        resourceRepository.setActiveById(event.resourceId(), StorageType.SERVER_AVATAR);
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(ServerBannerDeletedEvent event) {
        resourceRepository.setDeletedById(event.resourceId());
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(ServerBannerUpdatedEvent event) {
        resourceRepository.setActiveById(event.resourceId(), StorageType.SERVER_BANNER);
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(ImageUploadedEvent event) {
        resourceRepository.setActiveById(event.resourceId(), StorageType.GALLERY_IMAGE);
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(ImageDeletedEvent event) {
        resourceRepository.setDeletedById(event.resourceId());
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(GalleryDeletedEvent event) {
        resourceRepository.setDeletedByIdBatch(event.resourceIds());
    }
}
