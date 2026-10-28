package vn.io.huangnosimp.nautilusserver.star.dto;

import lombok.Builder;

@Builder
public record UserStaredInfo(String username, String avatarUrl) {}
