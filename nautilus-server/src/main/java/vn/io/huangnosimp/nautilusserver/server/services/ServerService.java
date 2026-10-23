package vn.io.huangnosimp.nautilusserver.server.services;

import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;

public interface ServerService {
    ServerInfoResponse getServerInfo(String username, String servername);
    void createServer(String username, CreateServerRequest body);
    void deleteServer(String username, String servername);
    void updateServerInfo(String username, String servername, UpdateServerInfoRequest body);

    void deleteServerAvatar(String username, String servername);

    void deleteServerBanner(String username, String servername);
}
