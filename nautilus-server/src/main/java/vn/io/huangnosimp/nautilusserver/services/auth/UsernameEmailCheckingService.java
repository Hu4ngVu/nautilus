package vn.io.huangnosimp.nautilusserver.services.auth;

public interface UsernameEmailCheckingService {
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
}
