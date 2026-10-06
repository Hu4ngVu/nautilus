package vn.io.huangnosimp.nautilusserver.exceptions.auth;

public class InvalidPassword extends RuntimeException {
    public InvalidPassword(String message) {
        super(message);
    }
}
