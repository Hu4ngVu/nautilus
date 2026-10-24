package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.util.Map;
import java.util.Set;

public record JavaVersionResponse(
        Map<String, Set<String>> javaVersions,
        String primaryJavaVersion
) {
}
