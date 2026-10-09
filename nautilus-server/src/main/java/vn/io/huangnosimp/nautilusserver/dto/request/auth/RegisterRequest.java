package vn.io.huangnosimp.nautilusserver.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "username is required")
        @Pattern(regexp = "^[a-zA-Z0-9_@.]+$", message = "Username can only contain letters, numbers, underscores, @ and .")
        String username,

        @NotBlank(message = "email is required")
        @Email
        String email,

        @NotBlank(message = "password is required")
        String password) {
}
