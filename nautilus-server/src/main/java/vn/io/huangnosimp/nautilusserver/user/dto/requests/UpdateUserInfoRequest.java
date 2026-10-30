package vn.io.huangnosimp.nautilusserver.user.dto.requests;

import jakarta.validation.constraints.Size;

public record UpdateUserInfoRequest(
        String resourceId,
        @Size(max = 10000, message = "Bio cannot exceed 10000 characters") String bio) {}
