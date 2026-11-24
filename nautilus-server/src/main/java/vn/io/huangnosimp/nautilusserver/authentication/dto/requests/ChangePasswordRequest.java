package vn.io.huangnosimp.nautilusserver.authentication.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "currentPassword is required") String currentPassword,
        @NotBlank(message = "newPassword is required")
                @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
                String newPassword) {}
