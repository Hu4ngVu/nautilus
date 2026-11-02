package vn.io.huangnosimp.nautilusserver.events;

import lombok.Builder;

@Builder
public record ServerCreatedEvent(String userId, String serverId, boolean isPublic) {}
