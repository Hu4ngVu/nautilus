package vn.io.huangnosimp.nautilusserver.server.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.api.AuthUserQuery;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.events.ServerDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.ServerVisibilityChangedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateServer;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.ChangeServerVisibility;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.server.models.BedrockMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.JavaMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;
import vn.io.huangnosimp.nautilusserver.server.repositories.BedrockMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.JavaMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.TagRepository;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {
    @Mock ServerRepository servers;
    @Mock ApplicationEventPublisher events;
    @Mock GalleryService gallery;
    @Mock ResourceStorage storage;
    @Mock AuthUserQuery authUserQuery;
    @Mock TagRepository tagRepository;
    @Mock JavaMcVersionRepository javaVersions;
    @Mock BedrockMcVersionRepository bedrockVersions;
    @InjectMocks ServerService service;

    private static final PageRequest PAGE = PageRequest.of(0, 20);

    private static Server server(String id, ServerPlatform platform, String bannerId) {
        Server server =
                Server.builder()
                        .userId("o1")
                        .servername("name-" + id)
                        .visibility(true)
                        .requiresPremium(false)
                        .type(ServerType.VANILLA)
                        .platform(platform)
                        .summary("summary")
                        .starCount(0)
                        .serverBannerId(bannerId)
                        .build();
        ReflectionTestUtils.setField(server, "id", id);
        return server;
    }

    private static ServerTag tag(Server server, String name) {
        return ServerTag.builder().server(server).tag(name).category(TagCategory.FEATURES).build();
    }

    private Page<ServerInfoResponse> listAll(List<Server> content) {
        when(servers.findAllByVisibilityIsTrue(any(), any())).thenReturn(new PageImpl<>(content));
        return service.getAllServer(null, null, null, null, PAGE);
    }

    @Test
    void listingMapsExtendedFieldsWithBatchedLookups() {
        Server s1 = server("s1", ServerPlatform.JAVA, "b1");
        Server s2 = server("s2", ServerPlatform.BEDROCK, null);
        Server s3 = server("s3", ServerPlatform.JAVA, null);
        lenient().when(storage.createSignedGetUrl("b1")).thenReturn("signed-b1");
        when(authUserQuery.getUsernamesByAuthUserIds(any())).thenReturn(Map.of("o1", "alice"));
        when(tagRepository.findAllByServerIdInOrderByCreatedAtAsc(any()))
                .thenReturn(List.of(tag(s1, "t1"), tag(s1, "t2"), tag(s1, "t3"), tag(s1, "t4")));
        when(javaVersions.findAllByServerIdInAndIsPrimaryMcVersionTrue(any()))
                .thenReturn(
                        List.of(
                                JavaMcVersion.builder()
                                        .server(s1)
                                        .mcVersion("1.21.1")
                                        .isPrimaryMcVersion(true)
                                        .build()));
        when(bedrockVersions.findAllByServerIdInAndIsPrimaryMcVersionTrue(any()))
                .thenReturn(
                        List.of(
                                BedrockMcVersion.builder()
                                        .server(s2)
                                        .mcVersion("1.21.40")
                                        .isPrimaryMcVersion(true)
                                        .build()));

        var content = listAll(List.of(s1, s2, s3)).getContent();

        var r1 = content.get(0);
        assertEquals("alice", r1.username());
        assertEquals("signed-b1", r1.bannerUrl());
        assertEquals(List.of("t1", "t2", "t3"), r1.tags());
        assertEquals("1.21.1", r1.primaryVersion());
        assertEquals(ServerPlatform.JAVA, r1.serverPlatform());
        assertEquals(ServerType.VANILLA, r1.serverType());
        assertEquals(true, r1.visibility());
        assertEquals(List.of(), content.get(1).tags());
        assertEquals("1.21.40", content.get(1).primaryVersion());
        assertNull(content.get(2).primaryVersion());
        verify(authUserQuery, times(1)).getUsernamesByAuthUserIds(any());
        verify(tagRepository, times(1)).findAllByServerIdInOrderByCreatedAtAsc(any());
        verify(javaVersions, times(1)).findAllByServerIdInAndIsPrimaryMcVersionTrue(any());
        verify(bedrockVersions, times(1)).findAllByServerIdInAndIsPrimaryMcVersionTrue(any());
        verify(authUserQuery, never()).getUsernameByAuthUserId(any());
    }

    @Test
    void listingSkipsBedrockLookupWhenPageHasNoBedrockServer() {
        when(authUserQuery.getUsernamesByAuthUserIds(any())).thenReturn(Map.of("o1", "alice"));
        listAll(List.of(server("s1", ServerPlatform.JAVA, null)));
        verify(bedrockVersions, never()).findAllByServerIdInAndIsPrimaryMcVersionTrue(any());
    }

    @Test
    void emptyPageDoesNoBatchLookups() {
        assertTrue(listAll(List.of()).isEmpty());
        verifyNoInteractions(authUserQuery, tagRepository, javaVersions, bedrockVersions);
    }

    @Test
    void listingFailsClearlyWhenOwnerMissing() {
        when(authUserQuery.getUsernamesByAuthUserIds(any())).thenReturn(Map.of());
        when(servers.findAllByVisibilityIsTrue(any(), any()))
                .thenReturn(new PageImpl<>(List.of(server("s1", ServerPlatform.JAVA, null))));
        assertThrows(UserNotFound.class, () -> service.getAllServer(null, null, null, null, PAGE));
    }

    @Test
    void ownerListingIncludesPrivateServersWithVisibilityFalse() {
        Server hidden = server("s1", ServerPlatform.JAVA, null);
        hidden.setVisibility(false);
        when(servers.findAllByUserId("o1", PAGE)).thenReturn(new PageImpl<>(List.of(hidden)));
        when(authUserQuery.getUsernamesByAuthUserIds(any())).thenReturn(Map.of("o1", "alice"));
        var result = service.getServerByUser("o1", "o1", PAGE).getContent().getFirst();
        assertEquals(false, result.visibility());
        verify(servers, never()).findAllByUserIdAndVisibilityIsTrue(any(), any());
    }

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
