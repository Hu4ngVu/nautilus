package vn.io.huangnosimp.nautilusserver.server.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImageDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImagesResponse;

public interface GalleryService {
    Page<ImagesResponse> getAllGallery(String userId, String servername, Pageable pageable);

    void uploadImage(String userId, String servername, UploadImageRequest body);

    void deleteImage(String userId, String servername, String imageId);

    void updateImageInfo(
            String userId, String servername, String imageId, UpdateImageInfoRequest body);

    void deleteGallery(String userId, String servername);

    ImageDetailResponse getImageDetail(String username, String servername, String imageId);
}
