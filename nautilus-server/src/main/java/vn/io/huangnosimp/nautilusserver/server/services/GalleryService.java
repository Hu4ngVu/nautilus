package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.List;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.GalleryResponse;

public interface GalleryService {
    List<GalleryResponse> getGallery(String userId, String servername);

    void uploadImage(String userId, String servername, UploadImageRequest body);

    void deleteImage(String userId, String servername, String imageId);

    void updateImageInfo(
            String userId, String servername, String imageId, UpdateImageInfoRequest body);

    void deleteGallery(String userId, String servername);
}
