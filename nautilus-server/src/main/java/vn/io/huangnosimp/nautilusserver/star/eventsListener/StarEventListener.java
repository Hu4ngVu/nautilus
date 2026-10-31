package vn.io.huangnosimp.nautilusserver.star.eventsListener;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;

@Service
@RequiredArgsConstructor
public class StarEventListener {
    private final StarRepository starRepository;

    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void on(ServerDeletedEvent event) {
        starRepository.deleteAllByServerId(event.serverId());
    }
}
