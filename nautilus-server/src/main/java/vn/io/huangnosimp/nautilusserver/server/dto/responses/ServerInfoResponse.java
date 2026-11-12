package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import lombok.Builder;

@Builder
public record ServerInfoResponse(
        String username,
        String servername,
        Boolean requiresPremium,
        String avatarUrl,
        String summary,
        Integer starCount) {}
