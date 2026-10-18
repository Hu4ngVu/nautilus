package vn.io.huangnosimp.nautilusserver.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateServerInfoRequest(
    MultipartFile banner,
    MultipartFile image,

    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9\\\\.]*[a-zA-Z0-9](:[0-9]{1,5})?$", message = "Invalid Java address format")
    @Size(max = 50, message = "Java address cannot exceed 50 characters")
    String javaAddress,

    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9\\\\.]*[a-zA-Z0-9](:[0-9]{1,5})?$", message = "Invalid Bedrock address format")
    @Size(max = 50, message = "Bedrock address cannot exceed 50 characters")
    String bedrockAddress,

    @Size(max = 10000, message = "Bio cannot exceed 10000 characters")
    String bio
) {
}
