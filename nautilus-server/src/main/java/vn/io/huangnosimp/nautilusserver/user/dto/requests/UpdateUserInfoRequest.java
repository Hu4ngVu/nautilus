package vn.io.huangnosimp.nautilusserver.user.dto.requests;

import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record UpdateUserInfoRequest(
        JsonNullable<String> resourceId,
        @Size(max = 10000, message = "Bio cannot exceed 10000 characters")
                JsonNullable<String> bio) {}
