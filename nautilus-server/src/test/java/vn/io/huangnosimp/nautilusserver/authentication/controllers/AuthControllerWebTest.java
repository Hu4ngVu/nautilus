package vn.io.huangnosimp.nautilusserver.authentication.controllers;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.authentication.dto.responses.AccessTokenResponse;
import vn.io.huangnosimp.nautilusserver.authentication.dto.results.TokenResult;
import vn.io.huangnosimp.nautilusserver.authentication.services.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebTest {
    @MockitoBean AuthService authService;
    @Autowired MockMvcTester mvc;

    @Test
    void registrationDelegatesValidatedPayload() {
        mvc.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"username":"alice","email":"alice@example.test","password":"secret"}
                        """)
                .assertThat()
                .hasStatusOk();
        verify(authService).registerUser(any());
    }

    @Test
    void invalidRegistrationDoesNotReachService() {
        mvc.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"username":"a","email":"not-email","password":"secret"}
                        """)
                .assertThat()
                .hasStatus4xxClientError();
        verifyNoInteractions(authService);
    }

    @Test
    void loginReturnsAccessToken() {
        when(authService.loginUser(any())).thenReturn(new TokenResult("access", "refresh"));
        mvc.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"username":"alice","password":"secret"}
                        """)
                .assertThat()
                .hasStatus(201)
                .bodyJson()
                .convertTo(AccessTokenResponse.class)
                .satisfies(
                        response ->
                                org.junit.jupiter.api.Assertions.assertEquals(
                                        "access", response.accessToken()));
    }
}
