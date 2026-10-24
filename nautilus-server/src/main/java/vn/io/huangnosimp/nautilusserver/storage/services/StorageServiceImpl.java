package vn.io.huangnosimp.nautilusserver.storage.services;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.io.huangnosimp.nautilusserver.storage.StorageService;
import vn.io.huangnosimp.nautilusserver.storage.exceptions.CannotUpload;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    private final S3Template s3Template;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Override
    public void upload(MultipartFile file, String key) {
        try {
            s3Template.upload(bucketName, key, file.getInputStream());
        } catch (Exception e) {
            throw new CannotUpload("Cannot upload file");
        }
    }

    @Override
    public String createSignedGetUrl(String key) {
        return s3Template.createSignedGetURL(bucketName, key, Duration.ofMinutes(1)).toString();
    }
}
