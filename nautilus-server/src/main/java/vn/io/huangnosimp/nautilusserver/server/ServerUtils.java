package vn.io.huangnosimp.nautilusserver.server;

import java.util.Optional;

public interface ServerUtils {
    Optional<ServerData> getPublicServerDataByServerId(String serverId);

    Optional<String> getPublicServerId(String userId, String servername);
}
