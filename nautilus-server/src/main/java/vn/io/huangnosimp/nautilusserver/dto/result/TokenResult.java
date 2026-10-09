package vn.io.huangnosimp.nautilusserver.dto.result;

public record TokenResult(
        String accessToken,
        String refreshToken) {
}
