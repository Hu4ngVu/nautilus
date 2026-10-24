package vn.io.huangnosimp.nautilusserver.server.dto.data;

import java.util.Map;
import java.util.Set;

public record VersionData(
        Map<String, Set<String>> versions,
        String primaryVersion
) {
}
