package vn.io.huangnosimp.nautilusserver.storage.eventlisteners;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@Service
@RequiredArgsConstructor
public class UserStorageEventListener {
    private final ResourceRepository resourceRepository;

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(UserAvatarDeletedEvent event) {
        resourceRepository.setDeletedById(event.resourceId());
    }

    @ApplicationModuleListener
    @Logging(level = Level.DEBUG, logArguments = true)
    public void on(UserAvatarUpdatedEvent event) {
        resourceRepository.setActiveById(event.resourceId(), StorageType.USER_AVATAR);
    }
}
