package vn.io.huangnosimp.nautilusserver.server.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImageDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ImagesResponse;

public interface GalleryService {
    Page<ImagesResponse> getAllGallery(String serverId, Pageable pageable);

    void uploadImage(String serverId, UploadImageRequest body);

    void deleteImage(String serverId, String imageId);

    void updateImageInfo(String serverId, String imageId, UpdateImageInfoRequest body);

    void deleteGallery(String serverId);

    ImageDetailResponse getImageDetail(String serverId, String imageId);
}
