package vn.io.huangnosimp.nautilusserver.server.eventslistener;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.event.Level;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.star.AddStarEvent;
import vn.io.huangnosimp.nautilusserver.star.DeleteStarEvent;

@Service
@RequiredArgsConstructor
public class ServerEventListener {
    private final ServerRepository serverRepository;

    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void on(AddStarEvent event) {
        Server server = getServer(event.serverId());
        serverRepository.increaseStarCount(server.getId(), 1);
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void on(DeleteStarEvent event) {
        Server server = getServer(event.serverId());
        serverRepository.decreaseStarCount(server.getId(), 1);
    }

    private @NonNull Server getServer(String serverId) {
        return serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFound(serverId));
    }
}
