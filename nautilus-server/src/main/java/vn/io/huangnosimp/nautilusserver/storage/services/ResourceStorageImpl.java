package vn.io.huangnosimp.nautilusserver.storage.services;

import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
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
    private final S3Template s3Template;
    private final ResourceRepository resourceRepository;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public String createSignedGetUrl(String resourceId) {
        var resource = resourceRepository.findById(resourceId);
        return resource.map(
                        value ->
                                s3Template
                                        .createSignedGetURL(
                                                bucketName, value.getId(), Duration.ofMinutes(5))
                                        .toString())
                .orElse(null);
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public boolean isResourceIdExist(String resourceId) {
        return resourceRepository.existsById(resourceId);
    }
}
