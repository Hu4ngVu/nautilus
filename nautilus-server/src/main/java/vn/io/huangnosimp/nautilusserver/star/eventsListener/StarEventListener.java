package vn.io.huangnosimp.nautilusserver.star.eventsListener;

import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.server.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;

@Service
@RequiredArgsConstructor
public class StarEventListener {
    private final StarRepository starRepository;

    @ApplicationModuleListener
    public void on(ServerDeletedEvent event) {
        starRepository.deleteAllByServerId(event.serverId());
    }
}
