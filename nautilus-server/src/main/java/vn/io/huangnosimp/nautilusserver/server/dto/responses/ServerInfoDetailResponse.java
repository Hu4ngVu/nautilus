package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.util.List;
import lombok.Builder;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;

@Builder
public record ServerInfoDetailResponse(
        String servername,
        Boolean visibility,
        Integer livePlayerCount,
        Integer maxPlayerCount,
        Boolean requiresPremium,
        String javaAddress,
        String bedrockAddress,
        String avatarUrl,
        String bannerUrl,
        String summary,
        String bio,
        List<String> tags,
        List<String> categories,
        List<String> javaVersions,
        List<String> bedrockVersions,
        ServerType serverType,
        ServerPlatform serverPlatform) {}
