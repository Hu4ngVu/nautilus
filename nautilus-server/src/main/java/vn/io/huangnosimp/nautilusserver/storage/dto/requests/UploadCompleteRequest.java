package vn.io.huangnosimp.nautilusserver.storage.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record UploadCompleteRequest(@NotBlank String resourceId) {}
