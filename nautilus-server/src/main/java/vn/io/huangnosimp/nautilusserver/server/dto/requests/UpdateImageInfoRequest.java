package vn.io.huangnosimp.nautilusserver.server.dto.requests;

public record UpdateImageInfoRequest(
        String resourceId, String title, String summary, Integer orderIndex) {}
