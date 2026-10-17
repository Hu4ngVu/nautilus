package vn.io.huangnosimp.nautilusserver.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class ServerAccessEvaluator {
    private final ServerRepository serverRepository;

    @Cacheable(
            cacheNames = "server-owner",
            key = "#servername + ':' + #authenticationName"
    )
    public boolean isOwner(String authenticationName, String servername) {
        return serverRepository.existsOwnerUsername(authenticationName, servername);
    }

    @Cacheable(
            cacheNames = "server-public",
            key = "#username + ':' + #servername"
    )
    public boolean isPublic(String username, String servername) {
        return Boolean.TRUE.equals(
                serverRepository.findVisibilityByNameAndUsername(username, servername)
        );
    }

    @Cacheable(
            cacheNames = "server-leader",
            key = "#servername + ':' + #authenticationName"
    )
    public boolean isLeader(String authenticationName, String servername) {
        return serverRepository.existsLeaderUsername(authenticationName, servername);
    }
}
