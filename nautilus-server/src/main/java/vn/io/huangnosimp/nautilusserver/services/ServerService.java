package vn.io.huangnosimp.nautilusserver.services;

import vn.io.huangnosimp.nautilusserver.dto.request.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.ServerInfoResponse;

public interface ServerService {
    ServerInfoResponse getServerInfo(String username, String servername);
    void createServer(String username, CreateServerRequest body);
    void deleteServer(String username, String servername);
    void updateServerInfo(String username, String servername, UpdateServerInfoRequest body);

    void deleteServerImage(String leaderName, String servername);

    void deleteServerBanner(String leaderName, String servername);
}
