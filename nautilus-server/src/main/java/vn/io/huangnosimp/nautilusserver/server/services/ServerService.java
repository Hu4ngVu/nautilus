package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.ChangeServerVisibility;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;

public interface ServerService {
    Page<ServerInfoResponse> getAllServer(
            Boolean requiresPremium,
            ServerType serverType,
            ServerPlatform serverPlatform,
            List<String> tags,
            Pageable pageable);

    ServerInfoDetailResponse getServerInfo(String serverId);

    void createServer(String authUserId, CreateServerRequest body);

    void deleteServer(String serverId);

    void updateServerInfo(String serverId, UpdateServerInfoRequest body);

    void deleteServerAvatar(String serverId);

    void deleteServerBanner(String serverId);

    void changeServername(String serverId, String newServername);

    void changeServerVisibility(String serverId, ChangeServerVisibility request);
}
