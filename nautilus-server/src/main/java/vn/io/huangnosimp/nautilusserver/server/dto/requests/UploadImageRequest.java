package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UploadImageRequest(
        @NotNull MultipartFile image,
        @Size(max = 64) String title,
        @Size(max = 255) String summary,
        Integer orderIndex) {}
