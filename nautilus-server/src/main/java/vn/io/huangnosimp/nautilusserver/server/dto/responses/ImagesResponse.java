package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ImagesResponse(
        String imageId,
        String imageUrl,
        String imageTitle,
        Integer orderIndex,
        Instant createdAt) {}
