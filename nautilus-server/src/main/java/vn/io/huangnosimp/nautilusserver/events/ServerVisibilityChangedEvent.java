package vn.io.huangnosimp.nautilusserver.events;

import lombok.Builder;

@Builder
public record ServerVisibilityChangedEvent(String serverId, boolean isPublic) {}
