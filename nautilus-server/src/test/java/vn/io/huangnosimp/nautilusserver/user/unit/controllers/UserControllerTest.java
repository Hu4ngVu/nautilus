package vn.io.huangnosimp.nautilusserver.user.unit.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.user.controllers.UserController;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.services.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @MockitoBean private UserService userService;

    @MockitoBean private GlobalIdResolver globalIdResolver;

    @Autowired private MockMvcTester mockMvcTester;

    @Test
    public void testGetUserInfo_ShouldReturnUserInfoWhenUsernameExists() {
        when(globalIdResolver.resolveUserId("testUser")).thenReturn("12345");
        when(userService.getUserInfo("12345"))
                .thenReturn(
                        UserInfoResponse.builder()
                                .username("testUser")
                                .serverCount(5)
                                .bio("Test bio")
                                .avatarUrl("https://example.com/avatar.jpg")
                                .build());
        mockMvcTester
                .get()
                .uri("/users/testUser")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(UserInfoResponse.class)
                .satisfies(
                        userInfoResponse -> {
                            assertEquals("testUser", userInfoResponse.username());
                            assertEquals(5, userInfoResponse.serverCount());
                            assertEquals("Test bio", userInfoResponse.bio());
                            assertEquals(
                                    "https://example.com/avatar.jpg", userInfoResponse.avatarUrl());
                        });
        verify(globalIdResolver).resolveUserId("testUser");
        verify(userService).getUserInfo("12345");
    }

    @Test
    void updateUserInfo_ShouldReturnOkAndDelegateToService() {
        when(globalIdResolver.resolveUserId("testUser")).thenReturn("12345");

        mockMvcTester
                .patch()
                .uri("/users/testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"resourceId":"avatar123","bio":"Updated bio"}
                        """)
                .assertThat()
                .hasStatusOk();

        ArgumentCaptor<UpdateUserInfoRequest> requestCaptor =
                ArgumentCaptor.forClass(UpdateUserInfoRequest.class);
        verify(globalIdResolver).resolveUserId("testUser");
        verify(userService).updateUserInfo(eq("12345"), requestCaptor.capture());
        assertEquals("avatar123", requestCaptor.getValue().resourceId().get());
        assertEquals("Updated bio", requestCaptor.getValue().bio().get());
    }

    @Test
    void updateUserInfo_ShouldReturnBadRequest_WhenBioExceedsMaximumLength() {
        String oversizedBio = "a".repeat(10001);

        mockMvcTester
                .patch()
                .uri("/users/testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bio\":\"" + oversizedBio + "\"}")
                .assertThat()
                .hasStatus4xxClientError();

        verify(userService, never()).updateUserInfo(any(), any(UpdateUserInfoRequest.class));
    }

    @Test
    void changeUsername_ShouldReturnOkAndDelegateToService() {
        when(globalIdResolver.resolveUserId("testUser")).thenReturn("12345");

        mockMvcTester
                .put()
                .uri("/users/testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"newUsername":"newUser"}
                        """)
                .assertThat()
                .hasStatusOk();

        verify(globalIdResolver).resolveUserId("testUser");
        verify(userService).changeUsername("12345", new ChangeUsernameRequest("newUser"));
    }
}
