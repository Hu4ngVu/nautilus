package vn.io.huangnosimp.nautilusserver.storage.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@Service
@RequiredArgsConstructor
public class ResourceStorageImpl implements ResourceStorage {
    private final ResourceRepository resourceRepository;

    // The bucket is served publicly from this base URL, so object URLs never expire
    @Value("${app.s3.public-url}")
    private String publicUrl;

    @Override
    public String getPublicUrl(String resourceId) {
        if (resourceId == null) {
            return null;
        }
        return publicUrl.replaceAll("/+$", "") + "/" + resourceId;
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public boolean isResourceIdExist(String resourceId) {
        return resourceRepository.existsById(resourceId);
    }
}
