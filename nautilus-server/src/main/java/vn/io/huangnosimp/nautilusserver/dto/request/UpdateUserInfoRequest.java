package vn.io.huangnosimp.nautilusserver.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UpdateUserInfoRequest(
        MultipartFile avatar,
        String bio
) {
}
