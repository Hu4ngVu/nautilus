package vn.io.huangnosimp.nautilusserver.server.dto.requests;

import java.util.Map;
import java.util.Set;

public record AddJavaVersionRequest(
        Map<String, Set<String>> javaVersions,
        String primaryJavaVersion
) {
}
