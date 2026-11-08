package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record UpdateServerInfoRequest(
        JsonNullable<String> avatarResourceId,
        JsonNullable<String> bannerResourceId,
        Boolean requiresPremium,
        @Pattern(
                        regexp = "^[a-zA-Z0-9][a-zA-Z0-9\\\\.]*[a-zA-Z0-9](:[0-9]{1,5})?$",
                        message = "Invalid Java address format")
                @Size(max = 50, message = "Java address cannot exceed 50 characters")
                JsonNullable<String> javaAddress,
        @Pattern(
                        regexp = "^[a-zA-Z0-9][a-zA-Z0-9\\\\.]*[a-zA-Z0-9](:[0-9]{1,5})?$",
                        message = "Invalid Bedrock address format")
                @Size(max = 50, message = "Bedrock address cannot exceed 50 characters")
                JsonNullable<String> bedrockAddress,
        @Size(max = 255, message = "Summary cannot exceed 255 characters")
                JsonNullable<String> summary,
        @Size(max = 10000, message = "Bio cannot exceed 10000 characters")
                JsonNullable<String> bio) {}
