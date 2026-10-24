package vn.io.huangnosimp.nautilusserver.storage.eventlistener;

import io.awspring.cloud.s3.S3Template;
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
import vn.io.huangnosimp.nautilusserver.server.DeleteImageEvent;
import vn.io.huangnosimp.nautilusserver.server.DeleteImagesBatchEvent;
import vn.io.huangnosimp.nautilusserver.server.DeleteServerAvatarEvent;
import vn.io.huangnosimp.nautilusserver.server.DeleteServerBannerEvent;
import vn.io.huangnosimp.nautilusserver.user.DeleteUserAvatar;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageEventListener {
    private final S3Template s3Template;
    private final S3Client s3Client;
    private static final int BATCH_SIZE = 1000;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    // from user
    @Retryable(
        value = {S3Exception.class}
    )
    @ApplicationModuleListener
    public void on(DeleteUserAvatar event) {
        s3Template.deleteObject(bucketName, event.key());
    }

    // from server
    @Retryable(
        value = {S3Exception.class}
    )
    @ApplicationModuleListener
    public void on(DeleteImageEvent event) {
        s3Template.deleteObject(bucketName, event.key());
    }

    @Retryable(
            value = {S3Exception.class}
    )
    @ApplicationModuleListener
    public void on(DeleteImagesBatchEvent event) {
        List<String> allKeys = event.keys();
        if (allKeys == null || allKeys.isEmpty()) {
            return;
        }
        for (int i = 0;i < allKeys.size(); i += BATCH_SIZE) {
            List<String> subList = allKeys.subList(i, Math.min(i + BATCH_SIZE, allKeys.size()));
            List<ObjectIdentifier> objectIdentifiers = subList.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();
            Delete delete = Delete.builder().objects(objectIdentifiers).quiet(true).build();
            DeleteObjectsRequest request = DeleteObjectsRequest.builder().delete(delete).bucket(bucketName).build();
            s3Client.deleteObjects(request);
        }
    }

    @Retryable(
            value = {S3Exception.class}
    )
    @ApplicationModuleListener
    public void on(DeleteServerBannerEvent event) {
        s3Template.deleteObject(bucketName, event.key());
    }

    @Retryable(
            value = {S3Exception.class}
    )
    @ApplicationModuleListener
    public void on(DeleteServerAvatarEvent event) {
        s3Template.deleteObject(bucketName, event.key());
    }
}
