package vn.io.huangnosimp.nautilusserver.auth.exceptions;

public class InvalidRefreshToken extends RuntimeException {
    public InvalidRefreshToken(String message) {
        super(message);
    }
}
