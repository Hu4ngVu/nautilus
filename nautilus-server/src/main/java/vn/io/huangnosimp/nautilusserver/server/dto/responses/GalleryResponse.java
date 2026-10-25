package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GalleryResponse(
        String imageId,
        String imageUrl,
        String imageTitle,
        String imageSummary,
        int orderIndex,
        Instant createdAt) {}
