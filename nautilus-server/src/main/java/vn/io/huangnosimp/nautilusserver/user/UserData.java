package vn.io.huangnosimp.nautilusserver.user;

import lombok.Builder;

@Builder
public record UserData(String authUserId, String username, String avatarKey) {}
