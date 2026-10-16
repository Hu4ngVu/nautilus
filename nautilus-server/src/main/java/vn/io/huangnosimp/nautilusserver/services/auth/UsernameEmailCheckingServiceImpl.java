package vn.io.huangnosimp.nautilusserver.services.auth;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RCuckooFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsernameEmailCheckingServiceImpl implements UsernameEmailCheckingService {
    @Qualifier("usernameCuckooFilter")
    private final RCuckooFilter<String> usernameCuckooFilter;
    @Qualifier("emailCuckooFilter")
    private final RCuckooFilter<String> emailCuckooFilter;

    @Override
    public boolean isUsernameExists(String username) {
        return usernameCuckooFilter.exists(username);
    }

    @Override
    public boolean isEmailExists(String email) {
        return emailCuckooFilter.exists(email);
    }
}
