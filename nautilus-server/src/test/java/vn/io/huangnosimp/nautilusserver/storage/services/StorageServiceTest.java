package vn.io.huangnosimp.nautilusserver.storage.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidContentType;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidWebhookSecretKey;
import vn.io.huangnosimp.nautilusserver.storage.dto.requests.UploadCompleteRequest;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {
    @Mock ResourceRepository resourceRepository;
    @Mock S3Template s3Template;
    StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(resourceRepository, s3Template);
        ReflectionTestUtils.setField(storageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(storageService, "webhookSecretKey", "secret");
    }

    @Test
    void uploadUrlRejectsUnsupportedTypesAndInvalidSizesBeforeCreatingResource() {
        assertThrows(
                InvalidContentType.class,
                () -> storageService.generateUploadUrl("text/plain", 10L));
        assertThrows(
                InvalidContentType.class,
                () -> storageService.generateUploadUrl("image/png", null));
        assertThrows(
                InvalidContentType.class, () -> storageService.generateUploadUrl("image/png", 0L));
        assertThrows(
                InvalidContentType.class,
                () -> storageService.generateUploadUrl("image/png", 10L * 1024 * 1024 + 1));
        verifyNoInteractions(resourceRepository, s3Template);
    }

    @Test
    void uploadCompletionRequiresConfiguredWebhookSecret() {
        assertThrows(
                InvalidWebhookSecretKey.class,
                () ->
                        storageService.completeUpload(
                                "wrong", new UploadCompleteRequest("resource")));
        verifyNoInteractions(resourceRepository);
    }

    @Test
    void validUploadCompletionMarksResourceUploaded() {
        storageService.completeUpload("secret", new UploadCompleteRequest("resource"));
        verify(resourceRepository).setUploadedById("resource");
    }
}
