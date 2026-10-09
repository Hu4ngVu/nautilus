package vn.io.huangnosimp.nautilusserver.exceptions.auth;

public class InvalidRefreshToken extends RuntimeException {
    public InvalidRefreshToken(String message) {
        super(message);
    }
}
