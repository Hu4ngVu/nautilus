package vn.io.huangnosimp.nautilusserver.authentication.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "newUsername is required")
                @Pattern(
                        regexp = "^[a-zA-Z0-9_@.]+$",
                        message =
                                "Username can only contain letters, numbers, underscores, @ and .")
                @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
                String username,
        @NotBlank(message = "email is required") @Email String email,
        @NotBlank(message = "password is required")
                @Size(max = 50, message = "Password cannot exceed 50 characters")
                String password) {}
