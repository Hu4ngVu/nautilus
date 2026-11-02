package vn.io.huangnosimp.nautilusserver.server.eventslistener;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.event.Level;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.events.AddStarEvent;
import vn.io.huangnosimp.nautilusserver.events.DeleteStarEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class StarServerEventListener {
    private final ServerRepository serverRepository;

    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    @CacheEvict(cacheNames = "serverInfoCache", key = "#event.serverId()")
    public void on(AddStarEvent event) {
        Server server = getServer(event.serverId());
        serverRepository.increaseStarCount(server.getId(), 1);
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    @CacheEvict(cacheNames = "serverInfoCache", key = "#event.serverId()")
    public void on(DeleteStarEvent event) {
        Server server = getServer(event.serverId());
        serverRepository.decreaseStarCount(server.getId(), 1);
    }

    private @NonNull Server getServer(String serverId) {
        return serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
    }
}
