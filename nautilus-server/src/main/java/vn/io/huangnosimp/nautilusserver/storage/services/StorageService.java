package vn.io.huangnosimp.nautilusserver.storage.services;

import vn.io.huangnosimp.nautilusserver.storage.dto.requests.UploadCompleteRequest;
import vn.io.huangnosimp.nautilusserver.storage.dto.responses.UploadUrlResponse;

public interface StorageService {
    UploadUrlResponse generateUploadUrl(String username, String contentType, Long contentLength);

    void completeUpload(String secretKey, UploadCompleteRequest request);
}
