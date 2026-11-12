package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.util.List;
import lombok.Builder;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;

@Builder
public record ServerInfoResponse(
        String username,
        String servername,
        Boolean requiresPremium,
        String avatarUrl,
        String bannerUrl,
        String summary,
        Integer starCount,
        Boolean visibility,
        ServerType serverType,
        ServerPlatform serverPlatform,
        List<String> tags,
        String primaryVersion) {}
