package vn.io.huangnosimp.nautilusserver.dto.request;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateUserInfoRequest(
        
        MultipartFile avatar,

        @Size(max = 10000, message = "Bio cannot exceed 10000 characters")
        String bio
) {
}
