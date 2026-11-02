package vn.io.huangnosimp.nautilusserver.authorization.services;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.authorization.AuthorizationUtils;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.ServerUtils;

@Service
@RequiredArgsConstructor
public class AuthorizationUtilsImpl implements AuthorizationUtils {
    private final OpenFgaClient openFgaClient;
    private final ServerUtils serverUtils;

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public boolean isEditor(String userId, String serverId) {
        return checkTuple(userId, serverId, "editor");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public boolean isViewer(String userId, String serverId) {
        return checkTuple(userId, serverId, "viewer");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public boolean isOwner(String userId, String serverId) {
        return checkTuple(userId, serverId, "owner");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    @Cacheable(cacheNames = CacheName.PUBLIC_SERVER_CACHE, key = "#serverId")
    public boolean isServerPublic(String serverId) {
        return serverUtils.isServerPublic(serverId);
    }

    private boolean checkTuple(String userId, String serverId, String relation) {
        ClientCheckRequest checkRequest =
                new ClientCheckRequest()
                        .user("user:" + userId)
                        .relation(relation)
                        ._object("server:" + serverId);
        try {
            var response = openFgaClient.check(checkRequest).join();
            return Objects.equals(response.getAllowed(), Boolean.TRUE);
        } catch (Exception e) {
            return false;
        }
    }
}
