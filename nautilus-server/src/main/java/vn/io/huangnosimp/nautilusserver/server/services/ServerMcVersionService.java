package vn.io.huangnosimp.nautilusserver.server.services;

import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddBedrockVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddJavaVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.BedrockVersionResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.JavaVersionResponse;

public interface ServerMcVersionService {
    JavaVersionResponse getAllJavaVersion(String username, String servername);
    BedrockVersionResponse getAllBedrockVersion(String username, String servername);
    void addJavaVersion(String username, String servername, AddJavaVersionRequest body);
    void addBedrockVersion(String username, String servername, AddBedrockVersionRequest body);
}
