package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.events.ServerData;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class ServerQueryImpl implements ServerQuery {
    private final ServerRepository serverRepository;
    private final ResourceStorage resourceStorage;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public Optional<ServerData> getPublicServerDataByServerId(String serverId) {
        return serverRepository
                .findByIdAndVisibilityIsTrue(serverId)
                .map(
                        server ->
                                ServerData.builder()
                                        .serverId(server.getId())
                                        .servername(server.getServername())
                                        .serverAvatarUrl(
                                                resourceStorage.createSignedGetUrl(
                                                        server.getServerAvatarId()))
                                        .serverTotalStar(server.getStarCount())
                                        .build());
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public Optional<String> getServerId(String userId, String servername) {
        return serverRepository.findServerId(userId, servername);
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public boolean isServerPublic(String serverId) {
        return serverRepository.existsByIdAndVisibilityIsTrue(serverId);
    }
}
