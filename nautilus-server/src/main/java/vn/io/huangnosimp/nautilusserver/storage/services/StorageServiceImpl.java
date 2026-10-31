package vn.io.huangnosimp.nautilusserver.storage.services;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidContentType;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidWebhookSecretKey;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageStatus;
import vn.io.huangnosimp.nautilusserver.storage.dto.requests.UploadCompleteRequest;
import vn.io.huangnosimp.nautilusserver.storage.dto.responses.UploadUrlResponse;
import vn.io.huangnosimp.nautilusserver.storage.models.Resource;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    private final ResourceRepository resourceRepository;
    private final AuthUserUtils authUserUtils;
    private final S3Template s3Template;

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${app.webhook.secret-key}")
    private String webhookSecretKey;

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public UploadUrlResponse generateUploadUrl(
            String username, String contentType, Long contentLength) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidContentType("Invalid content type: " + contentType);
        }
        if (contentLength == null || contentLength > MAX_FILE_SIZE || contentLength <= 0) {
            throw new InvalidContentType("Invalid file size: " + contentLength);
        }
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(
                                () ->
                                        new UserNotFound(
                                                "User not found with username: " + username));
        var storage =
                Resource.builder().userId(userId).storageStatus(StorageStatus.PENDING).build();
        resourceRepository.save(storage);
        ObjectMetadata metadata =
                ObjectMetadata.builder()
                        .contentType(contentType)
                        .contentLength(contentLength)
                        .build();
        String uploadUrl =
                s3Template
                        .createSignedPutURL(
                                bucketName, storage.getId(), Duration.ofMinutes(1), metadata, null)
                        .toString();
        return new UploadUrlResponse(uploadUrl, storage.getId());
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void completeUpload(String secretKey, UploadCompleteRequest request) {
        if (!secretKey.equals(webhookSecretKey)) {
            throw new InvalidWebhookSecretKey("Invalid webhook secret key");
        }
        String resourceId = request.resourceId();
        resourceRepository.setUploadedById(resourceId);
    }
}
