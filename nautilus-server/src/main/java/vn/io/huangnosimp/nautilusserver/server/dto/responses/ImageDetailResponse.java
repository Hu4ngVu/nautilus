package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ImageDetailResponse(
        String imageId,
        String imageUrl,
        String imageTitle,
        String summary,
        Integer orderIndex,
        Instant createdAt) {}
