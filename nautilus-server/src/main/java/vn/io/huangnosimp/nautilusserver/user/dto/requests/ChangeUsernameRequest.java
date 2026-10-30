package vn.io.huangnosimp.nautilusserver.user.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangeUsernameRequest(
        @NotBlank
                @NotBlank(message = "newUsername is required")
                @Pattern(
                        regexp = "^[a-zA-Z0-9_@.]+$",
                        message =
                                "Username can only contain letters, numbers, underscores, @ and .")
                @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
                String newUsername) {}
