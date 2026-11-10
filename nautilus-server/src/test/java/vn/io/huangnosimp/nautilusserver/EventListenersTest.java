package vn.io.huangnosimp.nautilusserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import vn.io.huangnosimp.nautilusserver.authentication.eventlisteners.UserAuthenticationEventListener;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.authorization.eventlistener.ServerAuthorizationEventListener;
import vn.io.huangnosimp.nautilusserver.authorization.services.AuthorizationService;
import vn.io.huangnosimp.nautilusserver.events.*;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.server.eventslistener.StarServerEventListener;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.eventlisteners.ScheduleStorageEventListener;
import vn.io.huangnosimp.nautilusserver.storage.eventlisteners.ServerStorageEventListener;
import vn.io.huangnosimp.nautilusserver.storage.eventlisteners.UserStorageEventListener;
import vn.io.huangnosimp.nautilusserver.storage.repositories.ResourceRepository;
import vn.io.huangnosimp.nautilusserver.user.eventslistener.AuthenticationUserEventListener;
import vn.io.huangnosimp.nautilusserver.user.eventslistener.ServerUserEventListener;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

class EventListenersTest {
    @Test
    void registrationCreatesDomainUserAndUsernameChangeUpdatesAuthRecord() {
        UserRepository userRepository = mock(UserRepository.class);
        new AuthenticationUserEventListener(userRepository)
                .on(new UserRegisteredEvent("auth-id", "alice"));
        verify(userRepository)
                .save(
                        argThat(
                                user ->
                                        user.getAuthUserId().equals("auth-id")
                                                && user.getUsername().equals("alice")
                                                && user.getServerCount() == 0));

        AuthUserRepository authRepository = mock(AuthUserRepository.class);
        new UserAuthenticationEventListener(authRepository)
                .on(new UsernameUpdatedEvent("auth-id", "renamed"));
        verify(authRepository).updateUsernameById("auth-id", "renamed");
    }

    @Test
    void serverEventsMaintainUserCountsWithoutDecrementingBelowZero() {
        UserRepository users = mock(UserRepository.class);
        ServerUserEventListener listener = new ServerUserEventListener(users);
        User user = User.builder().authUserId("auth-id").serverCount(1).build();
        when(users.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        listener.on(
                ServerCreatedEvent.builder()
                        .userId("auth-id")
                        .serverId("server-id")
                        .isPublic(true)
                        .build());
        listener.on(ServerDeletedEvent.builder().userId("auth-id").serverId("server-id").build());
        verify(users).increaseServerCount("auth-id", 1);
        verify(users).decreaseServerCount("auth-id", 1);

        User empty = User.builder().authUserId("empty").serverCount(0).build();
        when(users.findByAuthUserId("empty")).thenReturn(Optional.of(empty));
        listener.on(ServerDeletedEvent.builder().userId("empty").serverId("server-id").build());
        verify(users, never()).decreaseServerCount("empty", 1);
        when(users.findByAuthUserId("missing")).thenReturn(Optional.empty());
        assertThrows(
                UserNotFound.class,
                () ->
                        listener.on(
                                ServerCreatedEvent.builder()
                                        .userId("missing")
                                        .serverId("server-id")
                                        .build()));
    }

    @Test
    void authorizationEventsCreateAndRemovePublicRelationsConditionally() {
        AuthorizationService authorization = mock(AuthorizationService.class);
        ServerAuthorizationEventListener listener =
                new ServerAuthorizationEventListener(authorization);
        listener.handleCreateOwnerRelation(
                ServerCreatedEvent.builder()
                        .userId("user")
                        .serverId("server")
                        .isPublic(false)
                        .build());
        listener.handleCreatePublicRelation(
                ServerCreatedEvent.builder()
                        .userId("user")
                        .serverId("private")
                        .isPublic(false)
                        .build());
        listener.handleCreatePublicRelation(
                ServerCreatedEvent.builder()
                        .userId("user")
                        .serverId("public")
                        .isPublic(true)
                        .build());
        listener.handleDeletePublicRelation(
                ServerDeletedEvent.builder().serverId("private").wasPublic(false).build());
        listener.handleDeletePublicRelation(
                ServerDeletedEvent.builder().serverId("public").wasPublic(true).build());
        listener.on(new ServerVisibilityChangedEvent("server", false));

        verify(authorization).createOwnerRelation("user", "server");
        verify(authorization).createPublicRelation("public");
        verify(authorization).deletePublicRelation("public");
        verify(authorization).deletePublicRelation("server");
        verifyNoMoreInteractions(authorization);
    }

    @Test
    void starEventsAdjustCounterOnlyForExistingServer() {
        ServerRepository servers = mock(ServerRepository.class);
        Server server = Server.builder().build();
        ReflectionTestUtils.setField(server, "id", "server-id");
        when(servers.findById("server-id")).thenReturn(Optional.of(server));
        StarServerEventListener listener = new StarServerEventListener(servers);
        listener.on(new AddStarEvent("server-id"));
        listener.on(new DeleteStarEvent("server-id"));
        verify(servers).increaseStarCount("server-id", 1);
        verify(servers).decreaseStarCount("server-id", 1);
    }

    @Test
    void storageEventsMapResourcesToExpectedLifecycleOperations() {
        ResourceRepository resources = mock(ResourceRepository.class);
        UserStorageEventListener users = new UserStorageEventListener(resources);
        users.on(new UserAvatarDeletedEvent("user-avatar"));
        users.on(new UserAvatarUpdatedEvent("user-avatar"));

        ServerStorageEventListener servers = new ServerStorageEventListener(resources);
        servers.on(new ServerAvatarUpdatedEvent("server-avatar"));
        servers.on(new ServerBannerUpdatedEvent("server-banner"));
        servers.on(new ImageUploadedEvent("image"));
        servers.on(new ImageDeletedEvent("image"));
        servers.on(new GalleryDeletedEvent(java.util.List.of("image-1", "image-2")));

        verify(resources).setDeletedById("user-avatar");
        verify(resources).setActiveById("user-avatar", StorageType.USER_AVATAR);
        verify(resources).setActiveById("server-avatar", StorageType.SERVER_AVATAR);
        verify(resources).setActiveById("server-banner", StorageType.SERVER_BANNER);
        verify(resources).setActiveById("image", StorageType.GALLERY_IMAGE);
        verify(resources).setDeletedById("image");
        verify(resources).setDeletedByIdBatch(java.util.List.of("image-1", "image-2"));
    }

    @Test
    void scheduledStorageCleanupDeletesExpiredRowsEvenWhenNoS3ObjectsExist() {
        ResourceRepository resources = mock(ResourceRepository.class);
        S3Client s3Client = mock(S3Client.class);
        when(resources.findAllTrashResourceId(any())).thenReturn(java.util.List.of());
        ScheduleStorageEventListener listener =
                new ScheduleStorageEventListener(resources, s3Client);
        ReflectionTestUtils.setField(listener, "bucketName", "test-bucket");

        listener.on(new DeleteTrashResourceEvent());

        verify(resources).deleteAllById(java.util.List.of());
        verifyNoInteractions(s3Client);
    }
}
