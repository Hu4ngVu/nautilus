package vn.io.huangnosimp.nautilusserver.exceptions;

public class DuplicateEmail extends RuntimeException {
    public DuplicateEmail(String message) {
        super(message);
    }
}
