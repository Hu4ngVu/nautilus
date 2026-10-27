package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import lombok.Builder;

@Builder
public record ServerInfoResponse(
        String servername,
        Integer livePlayerCount,
        Integer maxPlayerCount,
        Boolean requiresPremium,
        String avatarUrl,
        String summary) {}
