package vn.io.huangnosimp.nautilusserver.auth.exceptions;

public class DuplicateUsername extends RuntimeException {
    public DuplicateUsername(String message) {
        super(message);
    }
}
