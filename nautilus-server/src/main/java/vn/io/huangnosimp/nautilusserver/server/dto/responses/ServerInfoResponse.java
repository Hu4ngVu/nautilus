package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record ServerInfoResponse(
        String servername,
        boolean visibility,
        int feedbackCount,
        int favoriteCount,
        int playerCount,
        int linkCount,
        boolean requiresPremium,
        String javaAddress,
        String bedrockAddress,
        String imageUrl,
        String bannerUrl,
        List<GalleryResponse> gallery
) {
}

