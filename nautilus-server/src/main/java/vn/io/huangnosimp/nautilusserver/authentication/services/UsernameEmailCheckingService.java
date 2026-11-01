package vn.io.huangnosimp.nautilusserver.authentication.services;

public interface UsernameEmailCheckingService {
    boolean isUsernameExists(String username);

    boolean isEmailExists(String email);
}
