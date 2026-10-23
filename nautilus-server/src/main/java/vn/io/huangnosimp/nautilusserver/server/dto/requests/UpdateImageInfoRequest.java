package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import org.springframework.web.multipart.MultipartFile;

public record UpdateImageInfoRequest(
        MultipartFile image,
        String title,
        String summary,
        Integer orderIndex
) {
}
