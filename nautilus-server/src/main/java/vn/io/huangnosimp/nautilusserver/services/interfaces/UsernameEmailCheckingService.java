package vn.io.huangnosimp.nautilusserver.services.interfaces;

public interface UsernameEmailCheckingService {
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
}
