package vn.io.huangnosimp.nautilusserver.services;

import vn.io.huangnosimp.nautilusserver.dto.request.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.GalleryResponse;

import java.util.List;

public interface GalleryService {
    List<GalleryResponse> getGallery(String leaderName, String servername);
    void uploadImage(String leaderName, String servername, UploadImageRequest body);
    void deleteImage(String leaderName, String servername, String imageId);
    void updateImageInfo(String leaderName, String servername, String imageId, UpdateImageInfoRequest body);
}
