package vn.io.huangnosimp.nautilusserver.storage.services;

import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageUtilsImpl implements StorageUtils {
    private final S3Template s3Template;
    private final ResourceRepository resourceRepository;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Override
    public String createSignedGetUrl(String modelId, StorageType storageType) {
        log.debug("Creating signed GET URL for modelId: {}, storageType: {}", modelId, storageType);
        if (resourceRepository.findId(modelId, storageType).isEmpty()) {
            return null;
        }
        var response =
                s3Template
                        .createSignedGetURL(
                                bucketName,
                                resourceRepository.findId(modelId, storageType).get(),
                                Duration.ofMinutes(5))
                        .toString();
        log.info(
                "Signed GET URL for modelId: {}, storageType: {} created successfully",
                modelId,
                storageType);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setResourceActive(
            String resourceId, String modelId, StorageType storageType, String userId) {
        log.debug(
                "Setting resource active for resourceId: {}, modelId: {}, storageType: {}, userId:"
                        + " {}",
                resourceId,
                modelId,
                storageType,
                userId);
        resourceRepository.setActiveById(resourceId, modelId, storageType, userId);
        log.info(
                "Resource active set for resourceId: {}, modelId: {}, storageType: {}, userId: {}",
                resourceId,
                modelId,
                storageType,
                userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setResourceDeleted(String modelId, StorageType storageType, String userId) {
        log.debug(
                "Setting resource deleted for modelId: {}, storageType: {}, userId: {}",
                modelId,
                storageType,
                userId);
        resourceRepository.setDeletedById(modelId, storageType, userId);
        log.info(
                "Resource deleted for modelId: {}, storageType: {}, userId: {}",
                modelId,
                storageType,
                userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setResourceDeletedBatch(
            List<String> modelIds, StorageType storageType, String userId) {
        log.debug(
                "Setting resource deleted batch for modelIds: {}, storageType: {}, userId: {}",
                modelIds,
                storageType,
                userId);
        resourceRepository.setDeletedByIdBatch(modelIds, storageType, userId);
        log.info(
                "Resource deleted batch for modelIds: {}, storageType: {}, userId: {}",
                modelIds,
                storageType,
                userId);
    }
}
