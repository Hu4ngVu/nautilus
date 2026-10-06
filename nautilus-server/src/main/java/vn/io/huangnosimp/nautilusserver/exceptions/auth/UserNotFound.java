package vn.io.huangnosimp.nautilusserver.exceptions.auth;

public class UserNotFound extends RuntimeException {
    public UserNotFound(String message) {
        super(message);
    }
}
