package vn.io.huangnosimp.nautilusserver.server.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidServerTag;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddTagRequest;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.TagRepository;

@ExtendWith(MockitoExtension.class)
class ServerTagServiceTest {
    @Mock ServerRepository servers;
    @Mock TagRepository tags;
    @InjectMocks ServerTagService service;

    @Test
    void listsTagsInStableCategoryAndTagOrder() {
        Server server = Server.builder().build();
        when(servers.findById("server-id")).thenReturn(Optional.of(server));
        when(tags.findAllByServer(server))
                .thenReturn(
                        List.of(
                                new ServerTag(server, "Skyblock", TagCategory.GAMEPLAY),
                                new ServerTag(server, "RPG", TagCategory.GAMEPLAY),
                                new ServerTag(server, "SMP", TagCategory.COMMUNITY)));

        var response = service.getAllServerTag("server-id");

        assertNotNull(response);
        assertEquals(
                List.of(TagCategory.COMMUNITY, TagCategory.GAMEPLAY),
                List.copyOf(response.tags().keySet()));
        assertEquals(
                List.of("RPG", "Skyblock"), List.copyOf(response.tags().get(TagCategory.GAMEPLAY)));
        verify(tags).findAllByServer(server);
    }

    @Test
    void rejectsUnknownServerAndInvalidTagsWithoutReplacingExistingTags() {
        when(servers.findById("missing")).thenReturn(Optional.empty());
        assertThrows(
                ServerNotFound.class,
                () -> service.addServerTag("missing", new AddTagRequest(Map.of())));

        Server server = Server.builder().build();
        when(servers.findById("server-id")).thenReturn(Optional.of(server));
        assertThrows(InvalidServerTag.class, () -> service.addServerTag("server-id", null));
        assertThrows(
                InvalidServerTag.class,
                () ->
                        service.addServerTag(
                                "server-id",
                                new AddTagRequest(
                                        Map.of(TagCategory.FEATURES, Set.of("not-a-tag")))));
        verify(tags, never()).deleteAllByServerId(any());
        verify(tags, never()).saveAll(any());
    }

    @Test
    void rejectsMoreThanTwentyTagsAndReplacesSetForValidTags() {
        Server server = Server.builder().build();
        ReflectionTestUtils.setField(server, "id", "server-id");
        when(servers.findById("server-id")).thenReturn(Optional.of(server));
        var tooMany =
                java.util.stream.IntStream.range(0, 21)
                        .mapToObj(i -> "not-used-" + i)
                        .collect(java.util.stream.Collectors.toSet());
        assertThrows(
                InvalidServerTag.class,
                () ->
                        service.addServerTag(
                                "server-id",
                                new AddTagRequest(Map.of(TagCategory.FEATURES, tooMany))));

        // The accepted tag set is taken from the enum's declared category map.
        var allowedTag =
                vn.io.huangnosimp.nautilusserver.server.constants.Tag.getTagMap()
                        .get(TagCategory.FEATURES)
                        .iterator()
                        .next();
        service.addServerTag(
                "server-id", new AddTagRequest(Map.of(TagCategory.FEATURES, Set.of(allowedTag))));
        verify(tags).deleteAllByServerId(server.getId());
        verify(tags)
                .saveAll(
                        argThat(
                                saved ->
                                        java.util.stream.StreamSupport.stream(
                                                                saved.spliterator(), false)
                                                        .count()
                                                == 1));
    }
}
