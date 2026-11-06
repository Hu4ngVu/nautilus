package vn.io.huangnosimp.nautilusserver.user.unit.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.user.controllers.UserController;
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
    }
}
