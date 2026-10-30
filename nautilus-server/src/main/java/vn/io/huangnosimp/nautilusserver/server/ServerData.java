package vn.io.huangnosimp.nautilusserver.server;

import lombok.Builder;

@Builder
public record ServerData(
        String serverId, String servername, Integer serverTotalStar, String serverAvatarUrl) {}
