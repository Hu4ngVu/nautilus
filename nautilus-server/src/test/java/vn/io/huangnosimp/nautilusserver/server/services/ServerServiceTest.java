package vn.io.huangnosimp.nautilusserver.server.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.events.ServerVisibilityChangedEvent;
import vn.io.huangnosimp.nautilusserver.events.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateServer;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.ChangeServerVisibility;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {
    @Mock ServerRepository servers;
    @Mock ApplicationEventPublisher events;
    @Mock GalleryService gallery;
    @InjectMocks ServerService service;

    @Test
    void duplicateServerNameIsRejectedBeforeSaveOrEvent() {
        when(servers.existsByUserIdAndServername("user", "my-server")).thenReturn(true);
        var request =
                new CreateServerRequest(
                        "my-server",
                        true,
                        false,
                        ServerType.VANILLA,
                        ServerPlatform.JAVA,
                        "summary");

        assertThrows(DuplicateServer.class, () -> service.createServer("user", request));
        verify(servers, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void visibilityChangeIsIdempotentAndPublishesEventWhenValueChanges() {
        Server server =
                Server.builder().userId("user").servername("server").visibility(true).build();
        ReflectionTestUtils.setField(server, "id", "server-id");
        when(servers.findById("server-id")).thenReturn(Optional.of(server));

        service.changeServerVisibility("server-id", new ChangeServerVisibility(true));
        verify(servers, never()).save(any());
        verifyNoInteractions(events);

        service.changeServerVisibility("server-id", new ChangeServerVisibility(false));
        assertFalse(server.getVisibility());
        verify(servers).save(server);
        verify(events).publishEvent(new ServerVisibilityChangedEvent("server-id", false));
    }

    @Test
    void deleteServerRemovesGalleryAndPublishesDeletionEvent() {
        Server server = Server.builder().userId("user").visibility(true).build();
        ReflectionTestUtils.setField(server, "id", "server-id");
        when(servers.findById("server-id")).thenReturn(Optional.of(server));

        service.deleteServer("server-id");

        verify(gallery).deleteGallery("server-id");
        verify(servers).delete(server);
        verify(events).publishEvent(any(ServerDeletedEvent.class));
    }

    @Test
    void deleteServerRejectsUnknownIdWithoutSideEffects() {
        when(servers.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ServerNotFound.class, () -> service.deleteServer("missing"));

        verifyNoInteractions(gallery, events);
        verify(servers, never()).delete(any(Server.class));
    }
}
