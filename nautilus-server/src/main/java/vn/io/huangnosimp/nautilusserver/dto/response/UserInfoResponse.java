package vn.io.huangnosimp.nautilusserver.dto.response;

public record UserInfoResponse(String username, String email, Integer serverCount, Integer collectionCount, String bio, String avatarUrl) {
}
