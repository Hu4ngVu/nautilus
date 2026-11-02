package vn.io.huangnosimp.nautilusserver.resolver;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.api.AuthUserQuery;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class GlobalIdResolver {
    private final AuthUserQuery authUserQuery;
    private final ServerQuery serverQuery;

    @Logging(level = Level.DEBUG, logArguments = true)
    @Cacheable(cacheNames = CacheName.USER_ID_CACHE, key = "#username")
    public String resolveUserId(String username) {
        return authUserQuery
                .getAuthUserIdByUsername(username)
                .orElseThrow(() -> new UserNotFound(username));
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Cacheable(cacheNames = CacheName.SERVER_ID_CACHE, key = "#username + ':' + #servername")
    public String resolveServerId(String username, String servername) {
        return serverQuery
                .getServerId(resolveUserId(username), servername)
                .orElseThrow(() -> new ServerNotFound(username + "/" + servername));
    }
}
