package vn.io.huangnosimp.nautilusserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.io.huangnosimp.nautilusserver.constants.ServerType;

public record CreateServerRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9_@.]+$", message = "Server name can only contain letters, numbers, underscores, @ and .")
        @Size(max = 50, message = "Server name cannot exceed 50 characters")
        String servername,

        @NotNull
        Boolean visibility,

        @NotNull
        Boolean requiresPremium,

        @NotNull
        ServerType type,

        @NotBlank
        @Size(max = 255, message = "Server summary cannot exceed 255 characters")
        String summary) {
}
