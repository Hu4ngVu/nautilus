package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.events.ServerData;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class ServerQueryImpl implements ServerQuery {
    private final ServerRepository serverRepository;
    private final ResourceStorage resourceStorage;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public Optional<ServerData> getPublicServerDataByServerId(String serverId) {
        return serverRepository.findByIdAndVisibilityIsTrue(serverId).map(this::toServerData);
    }

    @Override
    @Logging(level = Level.DEBUG)
    public Map<String, ServerData> getPublicServerDataByServerIds(Collection<String> serverIds) {
        if (serverIds.isEmpty()) return Map.of();
        return serverRepository.findAllByIdInAndVisibilityIsTrue(Set.copyOf(serverIds)).stream()
                .map(this::toServerData)
                .collect(Collectors.toMap(ServerData::serverId, Function.identity()));
    }

    @Override
    @Logging(level = Level.DEBUG)
    public List<String> filterPublicServerIds(Collection<String> serverIds) {
        if (serverIds.isEmpty()) return List.of();
        return serverRepository.findPublicIdsByIdIn(Set.copyOf(serverIds));
    }

    private ServerData toServerData(Server server) {
        return ServerData.builder()
                .serverId(server.getId())
                .ownerUserId(server.getUserId())
                .servername(server.getServername())
                .serverAvatarUrl(resourceStorage.getPublicUrl(server.getServerAvatarId()))
                .serverTotalStar(server.getStarCount())
                .build();
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
