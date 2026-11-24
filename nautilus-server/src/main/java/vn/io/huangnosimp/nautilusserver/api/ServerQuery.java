package vn.io.huangnosimp.nautilusserver.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vn.io.huangnosimp.nautilusserver.events.ServerData;

public interface ServerQuery {
    Optional<ServerData> getPublicServerDataByServerId(String serverId);

    /** Public servers only; private or missing ids are absent from the map. */
    Map<String, ServerData> getPublicServerDataByServerIds(Collection<String> serverIds);

    List<String> filterPublicServerIds(Collection<String> serverIds);

    Optional<String> getServerId(String userId, String servername);

    boolean isServerPublic(String serverId);
}
