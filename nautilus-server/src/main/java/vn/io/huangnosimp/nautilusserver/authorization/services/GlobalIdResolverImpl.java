package vn.io.huangnosimp.nautilusserver.authorization.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.authentication.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.authorization.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.ServerUtils;

@Service
@RequiredArgsConstructor
public class GlobalIdResolverImpl implements GlobalIdResolver {
    private final AuthUserUtils authUserUtils;
    private final ServerUtils serverUtils;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Cacheable(cacheNames = CacheName.USER_ID_CACHE, key = "#username")
    public String resolveUserId(String username) {
        return authUserUtils
                .getAuthUserIdByUsername(username)
                .orElseThrow(() -> new UserNotFound(username));
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Cacheable(cacheNames = CacheName.SERVER_ID_CACHE, key = "#username + ':' + #servername")
    public String resolveServerId(String username, String servername) {
        return serverUtils
                .getServerId(resolveUserId(username), servername)
                .orElseThrow(() -> new ServerNotFound(username + "/" + servername));
    }
}
