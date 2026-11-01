package vn.io.huangnosimp.nautilusserver.authorization.eventlistener;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.authorization.services.AuthorizationService;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.ServerCreatedEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.server.ServerVisibilityChangedEvent;

@Service
@RequiredArgsConstructor
public class AuthEventListener {
    private final AuthorizationService authorizationService;

    @Retryable
    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void handleCreateOwnerRelation(ServerCreatedEvent event) {
        authorizationService.createOwnerRelation(event.userId(), event.serverId());
    }

    @Retryable
    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void handleDeleteOwnerRelation(ServerDeletedEvent event) {
        authorizationService.deleteOwnerRelation(event.userId(), event.serverId());
    }

    @Retryable
    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void handleCreatePublicRelation(ServerCreatedEvent event) {
        if (event.isPublic()) {
            authorizationService.createPublicRelation(event.serverId());
        }
    }

    @Retryable
    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void handleDeletePublicRelation(ServerDeletedEvent event) {
        if (event.wasPublic()) {
            authorizationService.deletePublicRelation(event.serverId());
        }
    }

    @Retryable
    @Logging(level = Level.DEBUG, logArguments = true)
    @ApplicationModuleListener
    public void on(ServerVisibilityChangedEvent event) {
        if (event.isPublic()) {
            authorizationService.createPublicRelation(event.serverId());
        } else {
            authorizationService.deletePublicRelation(event.serverId());
        }
    }
}
