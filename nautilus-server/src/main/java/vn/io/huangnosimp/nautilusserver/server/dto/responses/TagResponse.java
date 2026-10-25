package vn.io.huangnosimp.nautilusserver.server.dto.responses;

import java.util.Map;
import java.util.Set;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;

public record TagResponse(Map<TagCategory, Set<String>> tags) {}
