package vn.io.huangnosimp.nautilusserver.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record GalleryResponse(
        String imageId,
        String imageUrl,
        String imageTitle,
        String imageSummary,
        int orderIndex,
        Instant createdAt
) {
}