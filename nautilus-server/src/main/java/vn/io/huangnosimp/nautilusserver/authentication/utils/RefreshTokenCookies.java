package vn.io.huangnosimp.nautilusserver.authentication.utils;

import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;

public final class RefreshTokenCookies {
    public static final String NAME = "refreshToken";
    public static final long MAX_AGE_SECONDS = 7 * 24 * 60 * 60; // 7 days

    private RefreshTokenCookies() {}

    public static ResponseCookie create(String refreshToken) {
        return build(refreshToken, MAX_AGE_SECONDS);
    }

    public static ResponseCookie clear() {
        return build("", 0); // Set maxAge to 0 to delete the cookie
    }

    private static ResponseCookie build(String refreshToken, long maxAge) {
        return ResponseCookie.from(NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite(Cookie.SameSite.LAX.name())
                .build();
    }
}
