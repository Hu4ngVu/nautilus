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
import org.springframework.context.ApplicationEventPublisher;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.events.UsernameUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateUsername;
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
                                new UpdateUserInfoRequest("id123", "New bio")));
    }

    @Test
    void updateUserInfo_ShouldUpdateUserInfoAndPublish2Events_WhenUserExistsAndUserHasOldAvatar() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId("avatar123")
                        .build();

        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));

        UpdateUserInfoRequest updateRequest =
                new UpdateUserInfoRequest("updatedAvatarId", "This is an updated bio.");

        userService.updateUserInfo("existingUserId", updateRequest);

        verify(eventPublisher).publishEvent(any(UserAvatarDeletedEvent.class));
        verify(eventPublisher).publishEvent(any(UserAvatarUpdatedEvent.class));

        assertEquals("updatedAvatarId", mockUser.getAvatarId());
        assertEquals("This is an updated bio.", mockUser.getBio());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserInfo_ShouldNotUpdate_WhenAllFieldsAreNull() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId("avatar123")
                        .build();

        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest(null, null);

        userService.updateUserInfo("existingUserId", updateRequest);

        assertEquals("avatar123", mockUser.getAvatarId());
        assertEquals("This is a test user.", mockUser.getBio());

        verify(userRepository, never()).findByAuthUserId("existingUserId");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserInfo_ShouldUpdateBioOnly_WhenAvatarIdIsNull() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId("avatar123")
                        .build();

        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));

        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest(null, "Updated bio only.");

        userService.updateUserInfo("existingUserId", updateRequest);

        assertEquals("avatar123", mockUser.getAvatarId());
        assertEquals("Updated bio only.", mockUser.getBio());

        verify(eventPublisher, never()).publishEvent(any(UserAvatarDeletedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(UserAvatarUpdatedEvent.class));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserInfo_ShouldUpdateAvatarOnly_WhenBioIsNull() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId("avatar123")
                        .build();

        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));

        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest("updatedAvatarId", null);

        userService.updateUserInfo("existingUserId", updateRequest);

        assertEquals("updatedAvatarId", mockUser.getAvatarId());
        assertEquals("This is a test user.", mockUser.getBio());

        verify(eventPublisher).publishEvent(any(UserAvatarDeletedEvent.class));
        verify(eventPublisher).publishEvent(any(UserAvatarUpdatedEvent.class));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserInfo_ShouldPublishOnlyUserAvatarUpdatedEvent_WhenUserHasNoOldAvatar() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId(null)
                        .build();

        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));

        UpdateUserInfoRequest updateRequest =
                new UpdateUserInfoRequest("updatedAvatarId", "Updated bio.");

        userService.updateUserInfo("existingUserId", updateRequest);

        verify(eventPublisher, never()).publishEvent(any(UserAvatarDeletedEvent.class));
        verify(eventPublisher).publishEvent(any(UserAvatarUpdatedEvent.class));

        assertEquals("updatedAvatarId", mockUser.getAvatarId());
        assertEquals("Updated bio.", mockUser.getBio());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUserAvatar_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByAuthUserId("nonexistentUserId")).thenReturn(Optional.empty());
        assertThrows(UserNotFound.class, () -> userService.deleteUserAvatar("nonexistentUserId"));
    }

    @Test
    void deleteUserAvatar_ShouldDeleteAvatarAndPublishEvent_WhenUserHasAvatar() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId("avatar123")
                        .build();
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));
        userService.deleteUserAvatar("existingUserId");
        assertNull(mockUser.getAvatarId());
        verify(eventPublisher).publishEvent(any(UserAvatarDeletedEvent.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUserAvatar_ShouldDoNothing_WhenUserHasNoAvatar() {
        User mockUser =
                User.builder()
                        .authUserId("existingUserId")
                        .username("testUser")
                        .serverCount(5)
                        .bio("This is a test user.")
                        .avatarId(null)
                        .build();
        when(userRepository.findByAuthUserId("existingUserId")).thenReturn(Optional.of(mockUser));
        userService.deleteUserAvatar("existingUserId");
        assertNull(mockUser.getAvatarId());
        verify(eventPublisher, never()).publishEvent(any(UserAvatarDeletedEvent.class));
        verify(userRepository, never()).save(any(User.class));
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
