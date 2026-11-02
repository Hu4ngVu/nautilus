package vn.io.huangnosimp.nautilusserver.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock private UserRepository userRepository;

    @Mock private ResourceStorage resourceStorage;

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

        assertThat(userInfoResponse).isNotNull();
        assertThat(userInfoResponse.username()).isEqualTo("testUser");
        assertThat(userInfoResponse.serverCount()).isEqualTo(5);
        assertThat(userInfoResponse.bio()).isEqualTo("This is a test user.");
        assertThat(userInfoResponse.avatarUrl()).isEqualTo("https://example.com/signed-url");
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

        assertThat(userInfoResponse).isNotNull();
        assertThat(userInfoResponse.username()).isEqualTo("testUser");
        assertThat(userInfoResponse.serverCount()).isEqualTo(5);
        assertThat(userInfoResponse.bio()).isEqualTo("This is a test user.");
        assertThat(userInfoResponse.avatarUrl()).isNull();
    }
}
