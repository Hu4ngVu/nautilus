package vn.io.huangnosimp.nautilusserver.storage.eventlistener;

import io.awspring.cloud.s3.S3Template;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;
import vn.io.huangnosimp.nautilusserver.server.*;
import vn.io.huangnosimp.nautilusserver.user.DeleteUserAvatarEvent;

@Service
@RequiredArgsConstructor
public class StorageEventListener {
    private final S3Template s3Template;
    private final S3Client s3Client;
    private static final int BATCH_SIZE = 1000;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    // from user
    @Retryable(value = {S3Exception.class})
    @ApplicationModuleListener
    public void on(DeleteUserAvatarEvent event) {
        s3Template.deleteObject(bucketName, event.key());
    }

    // from server
    @Retryable(value = {S3Exception.class})
    @ApplicationModuleListener
    public void on(DeleteImageEvent event) {
        s3Template.deleteObject(bucketName, event.key());
    }

    @Retryable(value = {S3Exception.class})
    @ApplicationModuleListener
    public void on(DeleteImagesBatchEvent event) {
        deleteBatch(event.keys());
    }

    @Retryable(value = {S3Exception.class})
    @ApplicationModuleListener
    public void on(DeleteServerBannerEvent event) {
        s3Template.deleteObject(bucketName, event.key());
    }

    @Retryable(value = {S3Exception.class})
    @ApplicationModuleListener
    public void on(DeleteServerAvatarEvent event) {
        s3Template.deleteObject(bucketName, event.key());
    }

    private void deleteBatch(List<String> allKeys) {
        if (allKeys == null || allKeys.isEmpty()) {
            return;
        }
        for (int i = 0; i < allKeys.size(); i += BATCH_SIZE) {
            List<String> subList = allKeys.subList(i, Math.min(i + BATCH_SIZE, allKeys.size()));
            List<ObjectIdentifier> objectIdentifiers =
                    subList.stream()
                            .map(key -> ObjectIdentifier.builder().key(key).build())
                            .toList();
            Delete delete = Delete.builder().objects(objectIdentifiers).quiet(true).build();
            DeleteObjectsRequest request =
                    DeleteObjectsRequest.builder().delete(delete).bucket(bucketName).build();
            s3Client.deleteObjects(request);
        }
    }
}
