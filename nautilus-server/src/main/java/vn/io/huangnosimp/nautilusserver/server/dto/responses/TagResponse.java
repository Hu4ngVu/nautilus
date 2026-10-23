package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;

import java.util.Map;
import java.util.Set;

public record TagResponse(
        Map<TagCategory, Set<String>> tags
) {
}
