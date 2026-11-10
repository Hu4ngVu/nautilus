package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record UploadImageRequest(
        @NotBlank String resourceId,
        @Size(max = 64) JsonNullable<String> title,
        @Size(max = 255) JsonNullable<String> summary,
        JsonNullable<Integer> orderIndex) {}
