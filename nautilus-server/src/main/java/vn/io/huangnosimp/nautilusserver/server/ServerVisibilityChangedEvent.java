package vn.io.huangnosimp.nautilusserver.server;

import lombok.Builder;

@Builder
public record ServerVisibilityChangedEvent(String serverId, boolean isPublic) {}
