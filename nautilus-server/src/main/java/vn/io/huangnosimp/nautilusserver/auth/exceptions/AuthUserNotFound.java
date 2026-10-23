package vn.io.huangnosimp.nautilusserver.auth.exceptions;

public class AuthUserNotFound extends RuntimeException {
    public AuthUserNotFound(String message) {
        super(message);
    }
}
