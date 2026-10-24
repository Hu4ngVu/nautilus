package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import java.util.Map;
import java.util.Set;

public record AddBedrockVersionRequest(
        Map<String, Set<String>> bedrockVersions,
        String primaryBedrockVersion
) {
}
