package vn.io.huangnosimp.nautilusserver.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterUser(
        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "password is required")
        String password) {
}
