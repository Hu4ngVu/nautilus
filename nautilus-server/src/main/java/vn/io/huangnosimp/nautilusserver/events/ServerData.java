package vn.io.huangnosimp.nautilusserver.events;

import lombok.Builder;

@Builder
public record ServerData(
        String serverId,
        String ownerUserId,
        String servername,
        Integer serverTotalStar,
        String serverAvatarUrl) {}
