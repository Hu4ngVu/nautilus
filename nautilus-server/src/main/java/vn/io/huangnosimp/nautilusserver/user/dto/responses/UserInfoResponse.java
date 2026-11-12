package vn.io.huangnosimp.nautilusserver.user.dto.responses;

import java.io.Serializable;
import lombok.Builder;

@Builder
public record UserInfoResponse(String username, int serverCount, String bio, String avatarUrl)
        implements Serializable {}
