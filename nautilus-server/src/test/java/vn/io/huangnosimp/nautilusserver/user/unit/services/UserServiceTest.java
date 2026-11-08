package vn.io.huangnosimp.nautilusserver.user.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
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
import vn.io.huangnosimp.nautilusserver.user.services.UserService;

@DisplayName("UserService Test")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock private UserRepository userRepository;

    @Mock private ResourceStorage resourceStorage;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private UserService userService;

    @Test
    void getUserInfo_ByAuthUserId_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByAuthUserId("nonexistentUserId")).thenReturn(Optional.empty());
        assertThrows(UserNotFound.class, () -> userService.getUserInfo("nonexistentUserId"));
    }

    @Test
    void getUserInfo_ByAuthUserId_ShouldReturnUserInfo() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId("avatar123")
                        .build();

        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));
        when(resourceStorage.createSignedGetUrl("avatar123"))
                .thenReturn("https://example.com/signed-url");

        var userInfoResponse = userService.getUserInfo("existingUserId");

        assertNotNull(userInfoResponse);
        assertEquals("testUser", userInfoResponse.username());
        assertEquals(5, userInfoResponse.serverCount());
        assertEquals("This is a test user.", userInfoResponse.bio());
        assertEquals("https://example.com/signed-url", userInfoResponse.avatarUrl());
    }

    @Test
    void getUserInfo_ByAuthUserId_ShouldReturnUserInfoWithNullAvatarUrl_WhenAvatarIdIsNull() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId(null)
                        .build();

        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));

        var userInfoResponse = userService.getUserInfo("existingUserId");

        assertNotNull(userInfoResponse);
        assertEquals("testUser", userInfoResponse.username());
        assertEquals(5, userInfoResponse.serverCount());
        assertEquals("This is a test user.", userInfoResponse.bio());
        assertNull(userInfoResponse.avatarUrl());
    }

    @Test
    void updateUserInfo_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByAuthUserId("nonexistentUserId")).thenReturn(Optional.empty());
        assertThrows(
                UserNotFound.class,
                () ->
                        userService.updateUserInfo(
                                "nonexistentUserId",
                                new UpdateUserInfoRequest(
                                        JsonNullable.undefined(), JsonNullable.of("New bio"))));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserInfo_ShouldUpdateBioOnly_WhenBioIsPresent() {
        User user = createUser("oldAvatar", "Old bio");
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(user));

        userService.updateUserInfo(
                "existingUserId",
                new UpdateUserInfoRequest(
                        JsonNullable.undefined(), JsonNullable.of("Updated bio")));

        assertEquals("oldAvatar", user.getAvatarId());
        assertEquals("Updated bio", user.getBio());
        verify(userRepository).save(user);
        verifyNoInteractions(resourceStorage, eventPublisher);
    }

    @Test
    void updateUserInfo_ShouldUpdateAvatarAndBio_WhenResourceExistsAndUserHasOldAvatar() {
        User user = createUser("oldAvatar", "Old bio");
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(user));
        when(resourceStorage.isResourceIdExist("newAvatar")).thenReturn(true);

        userService.updateUserInfo(
                "existingUserId",
                new UpdateUserInfoRequest(
                        JsonNullable.of("newAvatar"), JsonNullable.of("Updated bio")));

        assertEquals("newAvatar", user.getAvatarId());
        assertEquals("Updated bio", user.getBio());
        verify(resourceStorage).isResourceIdExist("newAvatar");
        verify(eventPublisher).publishEvent(new UserAvatarDeletedEvent("oldAvatar"));
        verify(eventPublisher).publishEvent(new UserAvatarUpdatedEvent("newAvatar"));
        verify(userRepository).save(user);
    }

    @Test
    void updateUserInfo_ShouldRemoveAvatar_WhenResourceIdIsNull() {
        User user = createUser("oldAvatar", "Old bio");
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(user));

        userService.updateUserInfo(
                "existingUserId",
                new UpdateUserInfoRequest(JsonNullable.of(null), JsonNullable.undefined()));

        assertNull(user.getAvatarId());
        assertEquals("Old bio", user.getBio());
        verify(eventPublisher).publishEvent(new UserAvatarDeletedEvent("oldAvatar"));
        verify(eventPublisher, never()).publishEvent(any(UserAvatarUpdatedEvent.class));
        verifyNoInteractions(resourceStorage);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserInfo_ShouldUpdateAvatarWithoutDeleteEvent_WhenUserHasNoOldAvatar() {
        User user = createUser(null, "Old bio");
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(user));
        when(resourceStorage.isResourceIdExist("newAvatar")).thenReturn(true);

        userService.updateUserInfo(
                "existingUserId",
                new UpdateUserInfoRequest(JsonNullable.of("newAvatar"), JsonNullable.undefined()));

        assertEquals("newAvatar", user.getAvatarId());
        verify(eventPublisher).publishEvent(new UserAvatarUpdatedEvent("newAvatar"));
        verify(eventPublisher, never()).publishEvent(any(UserAvatarDeletedEvent.class));
        verify(userRepository).save(user);
    }

    @Test
    void updateUserInfo_ShouldThrowInvalidResourceId_WhenResourceDoesNotExist() {
        User user = createUser("oldAvatar", "Old bio");
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(user));
        when(resourceStorage.isResourceIdExist("missingAvatar")).thenReturn(false);

        assertThrows(
                InvalidResourceId.class,
                () ->
                        userService.updateUserInfo(
                                "existingUserId",
                                new UpdateUserInfoRequest(
                                        JsonNullable.of("missingAvatar"),
                                        JsonNullable.undefined())));

        assertEquals("oldAvatar", user.getAvatarId());
        verify(resourceStorage).isResourceIdExist("missingAvatar");
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher).publishEvent(new UserAvatarDeletedEvent("oldAvatar"));
        verify(eventPublisher, never()).publishEvent(any(UserAvatarUpdatedEvent.class));
    }

    private User createUser(String avatarId, String bio) {
        return User.builder()
                .authUserId("existingUserId")
                .username("testUser")
                .serverCount(5)
                .bio(bio)
                .avatarId(avatarId)
                .build();
    }

    @Test
    void changeUsername_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByAuthUserId("nonexistentUserId")).thenReturn(Optional.empty());
        assertThrows(
                UserNotFound.class,
                () ->
                        userService.changeUsername(
                                "nonexistentUserId", new ChangeUsernameRequest("newUsername")));
    }

    @Test
    void
            changeUsername_ShouldUpdateUsernameAndPublishEvent_WhenUserExistsAndNewUsernameDoesNotAlreadyExist() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId(null)
                        .build();
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByUsername("newUsername")).thenReturn(false);
        userService.changeUsername("existingUserId", new ChangeUsernameRequest("newUsername"));
        assertEquals("newUsername", mockUser.getUsername());
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UsernameUpdatedEvent.class));
    }

    @Test
    void changeUsername_ShouldThrowException_WhenNewUsernameAlreadyExists() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId(null)
                        .build();
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByUsername("existingUsername")).thenReturn(true);
        assertThrows(
                DuplicateUsername.class,
                () ->
                        userService.changeUsername(
                                "existingUserId", new ChangeUsernameRequest("existingUsername")));
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UsernameUpdatedEvent.class));
    }
}
