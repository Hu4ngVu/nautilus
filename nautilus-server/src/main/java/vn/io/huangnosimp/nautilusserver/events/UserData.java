package vn.io.huangnosimp.nautilusserver.events;

import lombok.Builder;

@Builder
public record UserData(String authUserId, String username, String avatarUrl) {}
