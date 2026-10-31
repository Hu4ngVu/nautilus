package vn.io.huangnosimp.nautilusserver.server;

import lombok.Builder;

@Builder
public record ServerCreatedEvent(String userId, String serverId, boolean isPublic) {}
