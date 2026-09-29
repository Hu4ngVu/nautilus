package exceptions.auth;

public class DuplicateUsername extends RuntimeException {
    public DuplicateUsername(String message) {
        super(message);
    }
}
