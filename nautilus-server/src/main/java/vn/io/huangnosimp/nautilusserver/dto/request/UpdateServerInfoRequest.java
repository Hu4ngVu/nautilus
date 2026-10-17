package vn.io.huangnosimp.nautilusserver.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UpdateServerInfoRequest(
    MultipartFile banner,
    MultipartFile image,
    String javaAddress,
    String bedrockAddress,
    String bio
) {
}
