package vn.io.huangnosimp.nautilusserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.authentication.services.AuthUserQueryImpl;
import vn.io.huangnosimp.nautilusserver.authorization.services.PermissionCheckerImpl;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.server.services.ServerQueryImpl;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;
import vn.io.huangnosimp.nautilusserver.storage.services.ResourceStorageImpl;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;
import vn.io.huangnosimp.nautilusserver.user.services.UserQueryImpl;

class QueryAdaptersTest {
    @Test
    void userQueryReturnsProfileDataAndMissingUsersAsEmpty() {
        UserRepository repository = mock(UserRepository.class);
        ResourceStorage storage = mock(ResourceStorage.class);
        var query = new UserQueryImpl(repository, storage);
        when(repository.findByAuthUserId("auth-id"))
                .thenReturn(
                        Optional.of(
                                User.builder()
                                        .authUserId("auth-id")
                                        .username("alice")
                                        .avatarId("avatar")
                                        .build()));
        when(storage.getPublicUrl("avatar")).thenReturn("public-avatar");
        var userData = query.getUserDataByAuthUserId("auth-id").orElseThrow();
        assertEquals("alice", userData.username());
        assertEquals("public-avatar", userData.avatarUrl());
        when(repository.findByAuthUserId("missing")).thenReturn(Optional.empty());
        assertTrue(query.getUserDataByAuthUserId("missing").isEmpty());
    }

    @Test
    void authQueryPassesRepositoryLookupThrough() {
        AuthUserRepository repository = mock(AuthUserRepository.class);
        when(repository.findIdByUsername("alice")).thenReturn(Optional.of("auth-id"));
        assertEquals(
                "auth-id",
                new AuthUserQueryImpl(repository).getAuthUserIdByUsername("alice").orElseThrow());
    }

    @Test
    void serverQueryMapsPublicDataAndDelegatesVisibilityLookups() {
        ServerRepository repository = mock(ServerRepository.class);
        var storage = mock(vn.io.huangnosimp.nautilusserver.api.ResourceStorage.class);
        var query = new ServerQueryImpl(repository, storage);
        Server server =
                Server.builder()
                        .userId("owner-id")
                        .servername("server")
                        .serverAvatarId("avatar")
                        .starCount(3)
                        .build();
        ReflectionTestUtils.setField(server, "id", "server-id");
        when(repository.findByIdAndVisibilityIsTrue("server-id")).thenReturn(Optional.of(server));
        when(storage.getPublicUrl("avatar")).thenReturn("public-avatar");
        var result = query.getPublicServerDataByServerId("server-id").orElseThrow();
        assertEquals("server-id", result.serverId());
        assertEquals("owner-id", result.ownerUserId());
        assertEquals("server", result.servername());
        assertEquals("public-avatar", result.serverAvatarUrl());
        assertEquals(3, result.serverTotalStar());
        when(repository.existsByIdAndVisibilityIsTrue("server-id")).thenReturn(true);
        assertTrue(query.isServerPublic("server-id"));
    }

    @Test
    void authQueryResolvesUsernamesInOneBatch() {
        AuthUserRepository repository = mock(AuthUserRepository.class);
        when(repository.findIdAndUsernameByIdIn(Set.of("u1", "u2")))
                .thenReturn(List.of(idUsername("u1", "alice"), idUsername("u2", "bob")));
        var result =
                new AuthUserQueryImpl(repository)
                        .getUsernamesByAuthUserIds(List.of("u1", "u2", "u1"));
        assertEquals(Map.of("u1", "alice", "u2", "bob"), result);
        verify(repository, times(1)).findIdAndUsernameByIdIn(any());
    }

    @Test
    void batchQueriesShortCircuitOnEmptyInput() {
        AuthUserRepository authUsers = mock(AuthUserRepository.class);
        UserRepository users = mock(UserRepository.class);
        ServerRepository servers = mock(ServerRepository.class);
        ResourceStorage storage = mock(ResourceStorage.class);
        assertTrue(new AuthUserQueryImpl(authUsers).getUsernamesByAuthUserIds(List.of()).isEmpty());
        assertTrue(new UserQueryImpl(users, storage).getUserDataByAuthUserIds(List.of()).isEmpty());
        var serverQuery = new ServerQueryImpl(servers, storage);
        assertTrue(serverQuery.getPublicServerDataByServerIds(List.of()).isEmpty());
        assertTrue(serverQuery.filterPublicServerIds(List.of()).isEmpty());
        verifyNoInteractions(authUsers, users, servers);
    }

    @Test
    void serverQueryBatchReturnsOnlyPublicServersWithOwner() {
        ServerRepository repository = mock(ServerRepository.class);
        ResourceStorage storage = mock(ResourceStorage.class);
        Server server =
                Server.builder()
                        .userId("owner-1")
                        .servername("Atlantis")
                        .serverAvatarId("avatar")
                        .starCount(3)
                        .build();
        ReflectionTestUtils.setField(server, "id", "s1");
        when(repository.findAllByIdInAndVisibilityIsTrue(Set.of("s1", "s2")))
                .thenReturn(List.of(server));
        when(storage.getPublicUrl("avatar")).thenReturn("public-avatar");
        var query = new ServerQueryImpl(repository, storage);

        var result = query.getPublicServerDataByServerIds(List.of("s1", "s2"));
        assertEquals("owner-1", result.get("s1").ownerUserId());
        assertEquals("Atlantis", result.get("s1").servername());
        assertEquals("public-avatar", result.get("s1").serverAvatarUrl());
        assertFalse(result.containsKey("s2"));

        when(repository.findPublicIdsByIdIn(Set.of("s1", "s2"))).thenReturn(List.of("s1"));
        assertEquals(List.of("s1"), query.filterPublicServerIds(List.of("s1", "s2")));
    }

    @Test
    void userQueryBatchSignsAvatarPerUser() {
        UserRepository repository = mock(UserRepository.class);
        ResourceStorage storage = mock(ResourceStorage.class);
        when(repository.findAllByAuthUserIdIn(Set.of("a", "b")))
                .thenReturn(
                        List.of(
                                User.builder()
                                        .authUserId("a")
                                        .username("alice")
                                        .avatarId("a1")
                                        .build(),
                                User.builder()
                                        .authUserId("b")
                                        .username("bob")
                                        .avatarId(null)
                                        .build()));
        when(storage.getPublicUrl("a1")).thenReturn("url1");
        var result =
                new UserQueryImpl(repository, storage).getUserDataByAuthUserIds(List.of("a", "b"));
        assertEquals("alice", result.get("a").username());
        assertEquals("url1", result.get("a").avatarUrl());
        assertEquals("bob", result.get("b").username());
        verify(repository, times(1)).findAllByAuthUserIdIn(any());
    }

    private static AuthUserRepository.IdUsername idUsername(String id, String username) {
        return new AuthUserRepository.IdUsername() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getUsername() {
                return username;
            }
        };
    }

    @Test
    void permissionCheckFailsClosedWhenOpenFgaIsUnavailable() throws Exception {
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        ServerQuery serverQuery = mock(ServerQuery.class);
        when(openFgaClient.check(any(ClientCheckRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("offline")));
        PermissionCheckerImpl checker = new PermissionCheckerImpl(openFgaClient, serverQuery);
        assertFalse(checker.isEditor("user-id", "server-id"));
        when(serverQuery.isServerPublic("server-id")).thenReturn(true);
        assertTrue(checker.isServerPublic("server-id"));
    }

    @Test
    void resourceStorageBuildsPublicUrlsWithoutLookingUpObjects() {
        ResourceRepository repository = mock(ResourceRepository.class);
        ResourceStorageImpl storage = new ResourceStorageImpl(repository);
        ReflectionTestUtils.setField(storage, "publicUrl", "https://cdn.example.test/");
        assertEquals("https://cdn.example.test/resource-id", storage.getPublicUrl("resource-id"));
        assertNull(storage.getPublicUrl(null));
        verify(repository, never()).findById(any());
        when(repository.existsById("resource-id")).thenReturn(true);
        assertTrue(storage.isResourceIdExist("resource-id"));
    }
}
