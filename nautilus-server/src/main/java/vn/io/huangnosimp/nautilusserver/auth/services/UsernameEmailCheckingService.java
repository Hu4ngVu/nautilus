package vn.io.huangnosimp.nautilusserver.auth.services;

public interface UsernameEmailCheckingService {
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
}
