package vn.io.huangnosimp.nautilusserver.user.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidResourceId;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.services.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerWebTest {
    @MockitoBean UserService userService;
    @MockitoBean GlobalIdResolver globalIdResolver;
    @Autowired MockMvcTester mvc;

    @Test
    void resolvesUsernameAndReturnsUserInfo() {
        when(globalIdResolver.resolveUserId("alice")).thenReturn("auth-id");
        when(userService.getUserInfo("auth-id"))
                .thenReturn(UserInfoResponse.builder().username("alice").serverCount(2).build());

        mvc.get()
                .uri("/users/alice")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(UserInfoResponse.class)
                .satisfies(
                        response -> {
                            assertEquals("alice", response.username());
                            assertEquals(2, response.serverCount());
                        });
        verify(userService).getUserInfo("auth-id");
    }

    @Test
    void validatesPatchPayloadBeforeCallingService() {
        mvc.patch()
                .uri("/users/alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bio\":\"" + "x".repeat(10_001) + "\"}")
                .assertThat()
                .hasStatus4xxClientError();
        verifyNoInteractions(userService);
    }

    @Test
    void attachingAResourceThatIsNotUploadedIsABadRequest() {
        when(globalIdResolver.resolveUserId("alice")).thenReturn("auth-id");
        doThrow(new InvalidResourceId("Resource ID does not exist: r"))
                .when(userService)
                .updateUserInfo(eq("auth-id"), any());
        mvc.patch()
                .uri("/users/alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resourceId\":\"r\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void renameValidatesNewUsernameBeforeCallingService() {
        mvc.put()
                .uri("/users/alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newUsername\":\"a b\"}")
                .assertThat()
                .hasStatus4xxClientError();
        verifyNoInteractions(userService);
    }

    @Test
    void renameRejectsUsernameTakenByClientRoute() {
        mvc.put()
                .uri("/users/alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newUsername\":\"settings\"}")
                .assertThat()
                .hasStatus4xxClientError();
        verifyNoInteractions(userService);
    }
}
