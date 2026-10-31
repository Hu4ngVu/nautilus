package vn.io.huangnosimp.nautilusserver.server;

import lombok.Builder;

@Builder
public record ServerDeletedEvent(String userId, String serverId, boolean wasPublic) {}
