package vn.io.huangnosimp.nautilusserver.storage.services;

import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@Service
@RequiredArgsConstructor
public class StorageUtilsImpl implements StorageUtils {
    private final S3Template s3Template;
    private final ResourceRepository resourceRepository;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public String createSignedGetUrl(String modelId, StorageType storageType) {
        if (resourceRepository.findId(modelId, storageType).isEmpty()) {
            return null;
        }
        return s3Template
                .createSignedGetURL(
                        bucketName,
                        resourceRepository.findId(modelId, storageType).get(),
                        Duration.ofMinutes(5))
                .toString();
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void setResourceActive(
            String resourceId, String modelId, StorageType storageType, String userId) {
        resourceRepository.setActiveById(resourceId, modelId, storageType, userId);
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void setResourceDeleted(String modelId, StorageType storageType, String userId) {
        resourceRepository.setDeletedById(modelId, storageType, userId);
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void setResourceDeletedBatch(
            List<String> modelIds, StorageType storageType, String userId) {
        resourceRepository.setDeletedByIdBatch(modelIds, storageType, userId);
    }
}
