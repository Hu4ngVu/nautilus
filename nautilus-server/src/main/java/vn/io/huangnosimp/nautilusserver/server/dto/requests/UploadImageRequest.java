package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadImageRequest(
        @NotBlank String resourceId,
        @Size(max = 64) String title,
        @Size(max = 255) String summary,
        Integer orderIndex) {}
