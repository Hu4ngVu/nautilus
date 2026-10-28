package vn.io.huangnosimp.nautilusserver.star.dto;

import lombok.Builder;

@Builder
public record ServerStaredInfo(
        String username, String servername, Integer serverTotalStar, String serverAvatarUrl) {}
