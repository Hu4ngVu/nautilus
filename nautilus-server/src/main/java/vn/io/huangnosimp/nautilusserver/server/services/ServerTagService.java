package vn.io.huangnosimp.nautilusserver.server.services;

import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddTagRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.TagResponse;

public interface ServerTagService {
    TagResponse getAllServerTag(String serverId);

    void addServerTag(String serverId, AddTagRequest body);
}
