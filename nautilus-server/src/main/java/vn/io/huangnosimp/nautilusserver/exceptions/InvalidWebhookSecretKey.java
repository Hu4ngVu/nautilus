package vn.io.huangnosimp.nautilusserver.exceptions;

public class InvalidWebhookSecretKey extends RuntimeException {
    public InvalidWebhookSecretKey(String message) {
        super(message);
    }
}
