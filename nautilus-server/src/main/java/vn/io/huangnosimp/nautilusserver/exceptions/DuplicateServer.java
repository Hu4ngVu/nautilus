package vn.io.huangnosimp.nautilusserver.exceptions;

public class DuplicateServer extends RuntimeException {
    public DuplicateServer(String message) {
        super(message);
    }
}
