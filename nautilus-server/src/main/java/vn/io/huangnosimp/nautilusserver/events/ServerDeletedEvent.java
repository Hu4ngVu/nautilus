package vn.io.huangnosimp.nautilusserver.events;

import lombok.Builder;

@Builder
public record ServerDeletedEvent(
        String userId,
        String serverId,
        boolean wasPublic,
        String serverAvatarId,
        String serverBannerId) {}
