package vn.io.huangnosimp.nautilusserver.server.services;

import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddBedrockVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddJavaVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.BedrockVersionResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.JavaVersionResponse;

public interface ServerMcVersionService {
    JavaVersionResponse getAllJavaVersion(String serverId);

    BedrockVersionResponse getAllBedrockVersion(String serverId);

    void addJavaVersion(String serverId, AddJavaVersionRequest body);

    void addBedrockVersion(String serverId, AddBedrockVersionRequest body);
}
