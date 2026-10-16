package vn.io.huangnosimp.nautilusserver.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class ServerAccessEvaluator {
    private final ServerRepository serverRepository;

    public boolean isOwner(String authenticationName, String username, String servername) {
        return authenticationName.equals(username)
                && serverRepository.existsByNameAndUserUsername(servername, username);
    }
    
    public boolean isPublic(String username, String servername) {
        return Boolean.TRUE.equals(
                serverRepository.findVisibilityByNameAndUserUsername(servername, username)
        );
    }
}
