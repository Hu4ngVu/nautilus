package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.util.Map;
import java.util.Set;

public record BedrockVersionResponse(
        Map<String, Set<String>> bedrockVersions, String primaryBedrockVersion) {}
