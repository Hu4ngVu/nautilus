package vn.io.huangnosimp.nautilusserver.storage;

import java.util.List;

public interface StorageUtils {
    String createSignedGetUrl(String modelId, StorageType storageType);

    void setResourceDeleted(String modelId, StorageType storageType, String userId);

    void setResourceActive(
            String resourceId, String modelId, StorageType storageType, String userId);

    void setResourceDeletedBatch(List<String> modelIds, StorageType storageType, String userId);
}
