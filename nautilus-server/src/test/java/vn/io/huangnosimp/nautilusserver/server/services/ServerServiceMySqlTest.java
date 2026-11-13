package vn.io.huangnosimp.nautilusserver.server.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.io.huangnosimp.nautilusserver.api.AuthUserQuery;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;
import vn.io.huangnosimp.nautilusserver.server.repositories.BedrockMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.JavaMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.TagRepository;

/** Runs the list/search filters against a real database; mocked repositories cannot catch them. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@EnableJpaAuditing
class ServerServiceMySqlTest {
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.4.13").withDatabaseName("nautilus_test");

    private static final PageRequest PAGE = PageRequest.of(0, 20);

    @Autowired ServerRepository serverRepository;
    @Autowired TagRepository tagRepository;
    @Autowired JavaMcVersionRepository javaMcVersionRepository;
    @Autowired BedrockMcVersionRepository bedrockMcVersionRepository;

    ServerService service;

    @BeforeEach
    void setUp() {
        AuthUserQuery authUserQuery = mock(AuthUserQuery.class);
        when(authUserQuery.getUsernamesByAuthUserIds(any())).thenReturn(Map.of("owner", "alice"));
        service =
                new ServerService(
                        serverRepository,
                        mock(ApplicationEventPublisher.class),
                        mock(GalleryService.class),
                        mock(ResourceStorage.class),
                        authUserQuery,
                        tagRepository,
                        javaMcVersionRepository,
                        bedrockMcVersionRepository);

        Server java = save("java-smp", ServerPlatform.JAVA, ServerType.VANILLA, true, true);
        Server bedrock =
                save("bedrock-pack", ServerPlatform.BEDROCK, ServerType.MODPACK, false, true);
        save("bedrock-hidden", ServerPlatform.BEDROCK, ServerType.MODPACK, false, false);
        tagRepository.save(new ServerTag(java, "SMP", TagCategory.COMMUNITY));
        tagRepository.save(new ServerTag(bedrock, "PvP", TagCategory.FEATURES));
    }

    private Server save(
            String name,
            ServerPlatform platform,
            ServerType type,
            boolean requiresPremium,
            boolean visible) {
        return serverRepository.save(
                Server.builder()
                        .userId("owner")
                        .servername(name)
                        .visibility(visible)
                        .requiresPremium(requiresPremium)
                        .type(type)
                        .platform(platform)
                        .summary("summary")
                        .starCount(0)
                        .build());
    }

    private static List<String> names(Iterable<ServerInfoResponse> page) {
        var names = new java.util.ArrayList<String>();
        page.forEach(server -> names.add(server.servername()));
        return names.stream().sorted().toList();
    }

    @Test
    void listWithoutFiltersReturnsOnlyPublicServers() {
        assertEquals(
                List.of("bedrock-pack", "java-smp"),
                names(service.getAllServer(null, null, null, null, PAGE)));
    }

    @Test
    void listFiltersByPlatform() {
        assertEquals(
                List.of("bedrock-pack"),
                names(service.getAllServer(null, null, ServerPlatform.BEDROCK, null, PAGE)));
        assertEquals(
                List.of("java-smp"),
                names(service.getAllServer(null, null, ServerPlatform.JAVA, null, PAGE)));
    }

    @Test
    void listFiltersByTypePremiumAndTags() {
        assertEquals(
                List.of("bedrock-pack"),
                names(service.getAllServer(null, ServerType.MODPACK, null, null, PAGE)));
        assertEquals(
                List.of("java-smp"), names(service.getAllServer(true, null, null, null, PAGE)));
        assertEquals(
                List.of("java-smp"),
                names(service.getAllServer(null, null, null, List.of("SMP"), PAGE)));
    }

    @Test
    void searchCombinesQueryWithFilters() {
        assertEquals(
                List.of("bedrock-pack"),
                names(service.searchServer("bedrock", null, null, null, null, PAGE)));
        assertEquals(
                List.of(),
                names(
                        service.searchServer(
                                "java", null, null, ServerPlatform.BEDROCK, null, PAGE)));
    }
}
