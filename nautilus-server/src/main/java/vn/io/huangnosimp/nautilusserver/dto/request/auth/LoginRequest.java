package vn.io.huangnosimp.nautilusserver.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[a-zA-Z0-9_@.]+$", message = "Username can only contain letters, numbers, underscores, @ and .")
        String username,

        @NotBlank(message = "Password is required")
        String password) {
}
