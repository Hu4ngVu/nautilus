package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangeServernameRequest(
        @NotBlank
                @Pattern(
                        regexp = "^[a-zA-Z0-9_@.]+$",
                        message =
                                "Server name can only contain letters, numbers, underscores, @ and"
                                        + " .")
                @Size(max = 50, message = "Server name cannot exceed 50 characters")
                String newServername) {}
