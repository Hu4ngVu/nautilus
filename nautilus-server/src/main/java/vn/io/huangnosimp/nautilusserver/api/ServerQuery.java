package vn.io.huangnosimp.nautilusserver.api;

import java.util.Optional;
import vn.io.huangnosimp.nautilusserver.events.ServerData;

public interface ServerQuery {
    Optional<ServerData> getPublicServerDataByServerId(String serverId);

    Optional<String> getServerId(String userId, String servername);

    boolean isServerPublic(String serverId);
}
