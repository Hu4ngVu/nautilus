package vn.io.huangnosimp.nautilusserver.user.exceptions;

public class UserNotFound extends RuntimeException {
    public UserNotFound(String message) {
        super(message);
    }
}
