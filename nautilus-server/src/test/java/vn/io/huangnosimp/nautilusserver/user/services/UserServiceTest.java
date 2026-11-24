package vn.io.huangnosimp.nautilusserver.user.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.ApplicationEventPublisher;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.events.UsernameUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidResourceId;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @Mock ResourceStorage resourceStorage;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks UserService userService;

    @Test
    void getsUserInfoAndSignsAvatarOnlyWhenPresent() {
        User user = user(null);
        user.setAvatarId("avatar-id");
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(resourceStorage.getPublicUrl("avatar-id")).thenReturn("public-url");

        var result = userService.getUserInfo("auth-id");

        assertEquals("alice", result.username());
        assertEquals(3, result.serverCount());
        assertEquals("bio", result.bio());
        assertEquals("public-url", result.avatarUrl());
        verify(resourceStorage).getPublicUrl("avatar-id");
    }

    @Test
    void returnsNullAvatarWithoutCallingStorage() {
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user(null)));

        assertNull(userService.getUserInfo("auth-id").avatarUrl());
        verifyNoInteractions(resourceStorage);
    }

    @Test
    void missingUserRaisesUserNotFound() {
        when(userRepository.findByAuthUserId("missing")).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.getUserInfo("missing"));
    }

    @Test
    void updatesBioAndReplacesAvatarPublishingBothLifecycleEvents() {
        User user = user("old-avatar");
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(resourceStorage.isResourceIdExist("new-avatar")).thenReturn(true);

        userService.updateUserInfo(
                "auth-id",
                new UpdateUserInfoRequest(
                        JsonNullable.of("new-avatar"), JsonNullable.of("new bio")));

        assertEquals("new-avatar", user.getAvatarId());
        assertEquals("new bio", user.getBio());
        verify(eventPublisher).publishEvent(new UserAvatarDeletedEvent("old-avatar"));
        verify(eventPublisher).publishEvent(new UserAvatarUpdatedEvent("new-avatar"));
        verify(userRepository).save(user);
    }

    @Test
    void clearsAvatarWhenExplicitlyNull() {
        User user = user("old-avatar");
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));

        userService.updateUserInfo(
                "auth-id",
                new UpdateUserInfoRequest(JsonNullable.of(null), JsonNullable.undefined()));

        assertNull(user.getAvatarId());
        verify(eventPublisher).publishEvent(new UserAvatarDeletedEvent("old-avatar"));
        verify(eventPublisher, never()).publishEvent(any(UserAvatarUpdatedEvent.class));
        verify(userRepository).save(user);
    }

    @Test
    void rejectsUnknownAvatarAndDoesNotSave() {
        User user = user("old-avatar");
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(resourceStorage.isResourceIdExist("unknown")).thenReturn(false);

        assertThrows(
                InvalidResourceId.class,
                () ->
                        userService.updateUserInfo(
                                "auth-id",
                                new UpdateUserInfoRequest(
                                        JsonNullable.of("unknown"), JsonNullable.undefined())));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changingUsernameRejectsDuplicatesAndPublishesEventOnSuccess() {
        User user = user(null);
        when(userRepository.findByAuthUserId("auth-id")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        assertThrows(
                DuplicateUsername.class,
                () -> userService.changeUsername("auth-id", new ChangeUsernameRequest("taken")));
        verify(userRepository, never()).save(any());

        when(userRepository.existsByUsername("new-alice")).thenReturn(false);
        userService.changeUsername("auth-id", new ChangeUsernameRequest("new-alice"));
        assertEquals("new-alice", user.getUsername());
        verify(userRepository).save(user);
        verify(eventPublisher)
                .publishEvent(new UsernameUpdatedEvent("auth-id", "alice", "new-alice"));
    }

    private static User user(String avatarId) {
        return User.builder()
                .authUserId("auth-id")
                .username("alice")
                .serverCount(3)
                .bio("bio")
                .avatarId(avatarId)
                .build();
    }
}
