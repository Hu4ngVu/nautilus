package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.server.ServerData;
import vn.io.huangnosimp.nautilusserver.server.ServerUtils;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerUtilsImpl implements ServerUtils {
    private final ServerRepository serverRepository;
    private final StorageUtils storageUtils;

    @Override
    public Optional<ServerData> getPublicServerDataByServerId(String serverId) {
        log.debug("Fetching public server data for serverId: {}", serverId);
        var response =
                serverRepository
                        .findByIdAndVisibilityIsTrue(serverId)
                        .map(
                                server ->
                                        ServerData.builder()
                                                .serverId(server.getId())
                                                .servername(server.getServername())
                                                .serverAvatarUrl(
                                                        storageUtils.createSignedGetUrl(
                                                                server.getId(),
                                                                StorageType.SERVER_AVATAR))
                                                .serverTotalStar(server.getStarCount())
                                                .build());
        log.info("Public server data for serverId: {} fetched successfully", serverId);
        return response;
    }

    @Override
    public Optional<String> getPublicServerId(String userId, String servername) {
        log.debug("Fetching public serverId for userId: {}, servername: {}", userId, servername);
        var response = serverRepository.findServerId(userId, servername);
        log.info(
                "Public serverId for userId: {}, servername: {} fetched successfully",
                userId,
                servername);
        return response;
    }
}
