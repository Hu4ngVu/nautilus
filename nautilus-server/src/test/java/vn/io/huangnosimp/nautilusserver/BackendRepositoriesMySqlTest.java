package vn.io.huangnosimp.nautilusserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Provider;
import vn.io.huangnosimp.nautilusserver.authentication.constants.Role;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.authentication.models.RefreshToken;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.RefreshTokenRepository;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.JavaMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;
import vn.io.huangnosimp.nautilusserver.server.repositories.GalleryRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.JavaMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.TagRepository;
import vn.io.huangnosimp.nautilusserver.star.models.Star;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageStatus;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.models.Resource;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@EnableJpaAuditing
class BackendRepositoriesMySqlTest {
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.4.13").withDatabaseName("nautilus_test");

    @Autowired UserRepository userRepository;
    @Autowired AuthUserRepository authUserRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired ServerRepository serverRepository;
    @Autowired GalleryRepository galleryRepository;
    @Autowired JavaMcVersionRepository javaMcVersionRepository;
    @Autowired TagRepository tagRepository;
    @Autowired StarRepository starRepository;
    @Autowired ResourceRepository resourceRepository;

    @BeforeEach
    void createUser() {
        userRepository.save(
                User.builder().authUserId("auth-id").username("alice").serverCount(2).build());
    }

    @Test
    void findsByAuthUserIdAndChecksUniqueUsername() {
        assertEquals(
                "alice", userRepository.findByAuthUserId("auth-id").orElseThrow().getUsername());
        assertTrue(userRepository.existsByUsername("alice"));
        assertFalse(userRepository.existsByUsername("missing"));
    }

    @Test
    void adjustsServerCountAndDoesNotAllowNegativeCount() {
        userRepository.increaseServerCount("auth-id", 3);
        assertEquals(5, userRepository.findByAuthUserId("auth-id").orElseThrow().getServerCount());

        userRepository.decreaseServerCount("auth-id", 2);
        assertEquals(3, userRepository.findByAuthUserId("auth-id").orElseThrow().getServerCount());

        userRepository.decreaseServerCount("auth-id", 4);
        assertEquals(3, userRepository.findByAuthUserId("auth-id").orElseThrow().getServerCount());
    }

    @Test
    void authUserQueriesFindIdsAndUpdateUsername() {
        AuthUser user =
                authUserRepository.save(
                        AuthUser.builder()
                                .username("alice-auth")
                                .email("alice-auth@example.test")
                                .password("hash")
                                .provider(Provider.LOCAL)
                                .role(Role.USER)
                                .enabled(true)
                                .build());
        assertEquals(user.getId(), authUserRepository.findIdByUsername("alice-auth").orElseThrow());
        assertTrue(authUserRepository.existsByEmail("alice-auth@example.test"));

        authUserRepository.updateUsernameById(user.getId(), "alice-renamed");
        assertEquals(
                user.getId(), authUserRepository.findIdByUsername("alice-renamed").orElseThrow());
        assertTrue(authUserRepository.findByUsername("alice-renamed").isPresent());

        RefreshToken token =
                refreshTokenRepository.save(
                        RefreshToken.builder()
                                .authUser(user)
                                .token("refresh-token")
                                .expiredAt(java.time.Instant.now().plusSeconds(60))
                                .build());
        assertEquals(
                token.getId(),
                refreshTokenRepository.findByToken("refresh-token").orElseThrow().getId());
        refreshTokenRepository.deleteByAuthUser(user);
        assertTrue(refreshTokenRepository.findByToken("refresh-token").isEmpty());
    }

    @Test
    void serverQueriesUpdateStarCountAndFindByOwnerNameAndVisibility() {
        Server server =
                serverRepository.save(
                        Server.builder()
                                .userId("server-owner")
                                .servername("public-server")
                                .visibility(true)
                                .requiresPremium(false)
                                .type(ServerType.VANILLA)
                                .platform(ServerPlatform.JAVA)
                                .summary("summary")
                                .starCount(1)
                                .build());

        assertEquals(
                server.getId(),
                serverRepository.findServerId("server-owner", "public-server").orElseThrow());
        assertTrue(serverRepository.findByIdAndVisibilityIsTrue(server.getId()).isPresent());
        assertEquals(
                1,
                serverRepository
                        .findAllByVisibilityIsTrue(
                                (_, _, criteriaBuilder) -> criteriaBuilder.conjunction(),
                                PageRequest.of(0, 10))
                        .getTotalElements());
        serverRepository.increaseStarCount(server.getId(), 3);
        assertEquals(4, serverRepository.findById(server.getId()).orElseThrow().getStarCount());
        serverRepository.decreaseStarCount(server.getId(), 2);
        assertEquals(2, serverRepository.findById(server.getId()).orElseThrow().getStarCount());
    }

    @Test
    void resourceQueriesAdvanceUploadLifecycleAndOnlyExposeUploadedResources() {
        Resource resource =
                resourceRepository.save(
                        Resource.builder().storageStatus(StorageStatus.PENDING).build());
        assertFalse(resourceRepository.existsById(resource.getId()));

        resourceRepository.setUploadedById(resource.getId());
        assertTrue(resourceRepository.existsById(resource.getId()));
        resourceRepository.setActiveById(resource.getId(), StorageType.GALLERY_IMAGE);
        assertFalse(resourceRepository.existsById(resource.getId()));
        assertEquals(
                StorageStatus.ACTIVE,
                resourceRepository.findById(resource.getId()).orElseThrow().getStorageStatus());
        assertEquals(
                StorageType.GALLERY_IMAGE,
                resourceRepository.findById(resource.getId()).orElseThrow().getStorageType());

        resourceRepository.setDeletedById(resource.getId());
        assertEquals(
                StorageStatus.DELETED,
                resourceRepository.findById(resource.getId()).orElseThrow().getStorageStatus());
    }

    @Test
    void serverChildRepositoriesFindAndDeleteByParent() {
        Server server =
                serverRepository.save(
                        Server.builder()
                                .userId("child-owner")
                                .servername("child-server")
                                .visibility(false)
                                .requiresPremium(false)
                                .type(ServerType.VANILLA)
                                .platform(ServerPlatform.BEDROCK)
                                .summary("summary")
                                .starCount(0)
                                .build());
        Image image =
                galleryRepository.save(Image.builder().server(server).imageId("image-id").build());
        assertEquals(
                image.getId(),
                galleryRepository.findByServerAndId(server, image.getId()).orElseThrow().getId());
        assertEquals(
                1,
                galleryRepository
                        .findAllByServer(server, PageRequest.of(0, 10))
                        .getTotalElements());

        JavaMcVersion version =
                javaMcVersionRepository.save(
                        JavaMcVersion.builder()
                                .server(server)
                                .mcVersion("1.20.4")
                                .isPrimaryMcVersion(true)
                                .build());
        assertEquals(
                version.getId(),
                javaMcVersionRepository.findAllByServer(server).getFirst().getId());

        tagRepository.save(new ServerTag(server, "SMP", TagCategory.COMMUNITY));
        assertEquals(1, tagRepository.findAllByServer(server).size());
        tagRepository.deleteAllByServerId(server.getId());
        assertTrue(tagRepository.findAllByServer(server).isEmpty());

        starRepository.save(Star.builder().userId("user-id").serverId(server.getId()).build());
        assertEquals(
                1,
                starRepository
                        .findAllByServerId(server.getId(), PageRequest.of(0, 10))
                        .getTotalElements());
        assertEquals(
                1,
                starRepository
                        .findAllByUserId("user-id", PageRequest.of(0, 10))
                        .getTotalElements());
        starRepository.deleteAllByServerId(server.getId());
        assertTrue(starRepository.findAllByUserId("user-id", PageRequest.of(0, 10)).isEmpty());
    }
}
