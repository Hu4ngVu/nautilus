package vn.io.huangnosimp.nautilusserver.events;

import java.util.List;

public record GalleryDeletedEvent(List<String> resourceIds) {}
