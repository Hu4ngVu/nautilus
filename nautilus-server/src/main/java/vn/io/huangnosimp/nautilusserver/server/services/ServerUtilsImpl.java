package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.ServerData;
import vn.io.huangnosimp.nautilusserver.server.ServerUtils;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;

@Service
@RequiredArgsConstructor
public class ServerUtilsImpl implements ServerUtils {
    private final ServerRepository serverRepository;
    private final StorageUtils storageUtils;

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
                                                storageUtils.createSignedGetUrl(
                                                        server.getId(), StorageType.SERVER_AVATAR))
                                        .serverTotalStar(server.getStarCount())
                                        .build());
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public Optional<String> getPublicServerId(String userId, String servername) {
        return serverRepository.findPublicServerId(userId, servername);
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public Optional<String> getServerId(String userId, String servername) {
        return serverRepository.findServerId(userId, servername);
    }
}
