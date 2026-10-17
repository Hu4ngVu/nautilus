package vn.io.huangnosimp.nautilusserver.dto.response;

public record UserInfoResponse(String username, String email, int serverCount, String bio, String avatarUrl) {
}
