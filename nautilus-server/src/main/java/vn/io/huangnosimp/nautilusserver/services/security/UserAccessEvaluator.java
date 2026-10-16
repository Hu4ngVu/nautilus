package vn.io.huangnosimp.nautilusserver.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccessEvaluator {
    public boolean isOwner (String authenticationName, String username) {
        return authenticationName.equals(username);
    }
}
