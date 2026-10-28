package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.server.ServerData;
import vn.io.huangnosimp.nautilusserver.server.ServerUtils;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class ServerUtilsImpl implements ServerUtils {
    private final ServerRepository serverRepository;

    @Override
    public Optional<ServerData> getPublicServerDataByServerId(String serverId) {
        return serverRepository
                .findByIdAndVisibilityIsTrue(serverId)
                .map(
                        server ->
                                ServerData.builder()
                                        .serverId(server.getId())
                                        .servername(server.getServername())
                                        .serverAvatarKey(server.getAvatarKey())
                                        .serverTotalStar(server.getStarCount())
                                        .build());
    }

    @Override
    public Optional<String> getPublicServerId(String userId, String servername) {
        return serverRepository.findServerId(userId, servername);
    }
}
