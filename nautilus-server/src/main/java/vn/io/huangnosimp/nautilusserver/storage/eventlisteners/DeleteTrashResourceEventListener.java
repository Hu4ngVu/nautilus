package vn.io.huangnosimp.nautilusserver.storage.eventlisteners;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import vn.io.huangnosimp.nautilusserver.schedule.DeleteTrashResourceEvent;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@Service
@RequiredArgsConstructor
public class DeleteTrashResourceEventListener {
    private final ResourceRepository resourceRepository;
    private final S3Client s3Client;

    private static final int EXPIRATION_HOURS = 24; // Set the expiration time in hours
    private static final int BATCH_SIZE = 1000; // Set the batch size for deletion

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @ApplicationModuleListener
    public void on(DeleteTrashResourceEvent event) {
        var targetTime = Instant.now().minus(EXPIRATION_HOURS, ChronoUnit.HOURS);
        var trashResourceIds = resourceRepository.findAllTrashResourceId(targetTime);
        deleteBatch(trashResourceIds);
        resourceRepository.deleteAllById(trashResourceIds);
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
