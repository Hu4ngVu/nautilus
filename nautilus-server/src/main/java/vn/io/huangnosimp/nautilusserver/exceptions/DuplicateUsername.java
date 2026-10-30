package vn.io.huangnosimp.nautilusserver.exceptions;

public class DuplicateUsername extends RuntimeException {
    public DuplicateUsername(String message) {
        super(message);
    }
}
