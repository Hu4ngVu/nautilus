package vn.io.huangnosimp.nautilusserver.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginUser(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password) {
}
