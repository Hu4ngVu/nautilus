package vn.io.huangnosimp.nautilusserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import io.awspring.cloud.s3.S3Template;
import java.net.URI;
import java.util.Optional;
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
import vn.io.huangnosimp.nautilusserver.storage.models.Resource;
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
        when(storage.createSignedGetUrl("avatar")).thenReturn("signed-avatar");
        var userData = query.getUserDataByAuthUserId("auth-id").orElseThrow();
        assertEquals("alice", userData.username());
        assertEquals("signed-avatar", userData.avatarUrl());
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
                Server.builder().servername("server").serverAvatarId("avatar").starCount(3).build();
        ReflectionTestUtils.setField(server, "id", "server-id");
        when(repository.findByIdAndVisibilityIsTrue("server-id")).thenReturn(Optional.of(server));
        when(storage.createSignedGetUrl("avatar")).thenReturn("signed-avatar");
        var result = query.getPublicServerDataByServerId("server-id").orElseThrow();
        assertEquals("server-id", result.serverId());
        assertEquals("server", result.servername());
        assertEquals("signed-avatar", result.serverAvatarUrl());
        assertEquals(3, result.serverTotalStar());
        when(repository.existsByIdAndVisibilityIsTrue("server-id")).thenReturn(true);
        assertTrue(query.isServerPublic("server-id"));
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
    void resourceStorageSignsExistingObjectsAndReturnsNullForMissingIds() throws Exception {
        ResourceRepository repository = mock(ResourceRepository.class);
        S3Template s3 = mock(S3Template.class);
        ResourceStorageImpl storage = new ResourceStorageImpl(s3, repository);
        ReflectionTestUtils.setField(storage, "bucketName", "bucket");
        Resource resource =
                Resource.builder()
                        .storageStatus(
                                vn.io.huangnosimp.nautilusserver.storage.constants.StorageStatus
                                        .ACTIVE)
                        .build();
        ReflectionTestUtils.setField(resource, "id", "resource-id");
        when(repository.findById("resource-id")).thenReturn(Optional.of(resource));
        when(s3.createSignedGetURL(eq("bucket"), eq("resource-id"), any()))
                .thenReturn(URI.create("https://example.test/signed").toURL());
        assertEquals("https://example.test/signed", storage.createSignedGetUrl("resource-id"));
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertNull(storage.createSignedGetUrl("missing"));
        when(repository.existsById("resource-id")).thenReturn(true);
        assertTrue(storage.isResourceIdExist("resource-id"));
    }
}
