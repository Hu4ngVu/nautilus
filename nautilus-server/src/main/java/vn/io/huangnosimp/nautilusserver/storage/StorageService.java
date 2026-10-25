package vn.io.huangnosimp.nautilusserver.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void uploadImage(MultipartFile file, String key);

    String createSignedGetUrl(String key);
}
