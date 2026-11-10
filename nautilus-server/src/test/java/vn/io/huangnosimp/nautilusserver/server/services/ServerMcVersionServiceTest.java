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
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidMcVersion;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddJavaVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddBedrockVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.models.JavaMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.BedrockMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.JavaMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@ExtendWith(MockitoExtension.class)
class ServerMcVersionServiceTest {
    @Mock ServerRepository servers;
    @Mock JavaMcVersionRepository javaVersions;
    @Mock BedrockMcVersionRepository bedrockVersions;
    @InjectMocks ServerMcVersionService service;

    @Test
    void rejectsUnknownServerAndVersionsOutsideKnownGroup() {
        when(servers.findById("missing")).thenReturn(Optional.empty());
        assertThrows(
                ServerNotFound.class,
                () -> service.addJavaVersion("missing", new AddJavaVersionRequest(Map.of(), null)));

        Server server = Server.builder().build();
        when(servers.findById("server")).thenReturn(Optional.of(server));
        assertThrows(
                InvalidMcVersion.class,
                () ->
                        service.addJavaVersion(
                                "server",
                                new AddJavaVersionRequest(Map.of("1.99", Set.of("1.99")), "1.99")));
        assertThrows(
                InvalidMcVersion.class,
                () ->
                        service.addJavaVersion(
                                "server",
                                new AddJavaVersionRequest(
                                        Map.of("1.20", Set.of("1.20.1")), "1.19.4")));
        verify(javaVersions, never()).deleteAll(any());
    }

    @Test
    void validVersionReplacementDeletesOldRowsAndMarksOnlyRequestedPrimary() {
        Server server = Server.builder().build();
        when(servers.findById("server")).thenReturn(Optional.of(server));
        when(javaVersions.findAllByServer(server)).thenReturn(List.of());

        service.addJavaVersion(
                "server",
                new AddJavaVersionRequest(Map.of("1.20", Set.of("1.20.1", "1.20.4")), "1.20.4"));

        verify(javaVersions).deleteAll(List.of());
        verify(javaVersions)
                .saveAll(
                        argThat(
                                versions ->
                                        java.util.stream.StreamSupport.stream(
                                                                        versions.spliterator(),
                                                                        false)
                                                                .count()
                                                        == 2
                                                && java.util.stream.StreamSupport.stream(
                                                                versions.spliterator(), false)
                                                        .filter(
                                                                JavaMcVersion
                                                                        ::getIsPrimaryMcVersion)
                                                        .map(JavaMcVersion::getMcVersion)
                                                        .toList()
                                                        .equals(List.of("1.20.4"))));
    }

    @Test
    void validBedrockVersionReplacementDeletesOldRowsAndMarksPrimary() {
        Server server = Server.builder().build();
        when(servers.findById("server")).thenReturn(Optional.of(server));
        when(bedrockVersions.findAllByServer(server)).thenReturn(List.of());

        service.addBedrockVersion(
                "server", new AddBedrockVersionRequest(Map.of("1.20", Set.of("1.20.1")), "1.20.1"));

        verify(bedrockVersions).deleteAll(List.of());
        verify(bedrockVersions)
                .saveAll(argThat(rows -> {
                    var saved = java.util.stream.StreamSupport.stream(rows.spliterator(), false).toList();
                    return saved.size() == 1
                            && saved.getFirst().getMcVersion().equals("1.20.1")
                            && saved.getFirst().getIsPrimaryMcVersion();
                }));
        verifyNoInteractions(javaVersions);
    }

    @Test
    void rejectsInvalidBedrockVersionPayloadWithoutReplacingRows() {
        Server server = Server.builder().build();
        when(servers.findById("server")).thenReturn(Optional.of(server));

        assertThrows(InvalidMcVersion.class, () -> service.addBedrockVersion("server", null));
        assertThrows(
                InvalidMcVersion.class,
                () -> service.addBedrockVersion(
                        "server", new AddBedrockVersionRequest(Map.of("1.20", Set.of("1.99")), "1.99")));

        verifyNoInteractions(bedrockVersions);
    }
}
