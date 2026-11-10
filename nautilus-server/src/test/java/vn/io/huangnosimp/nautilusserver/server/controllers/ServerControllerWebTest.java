package vn.io.huangnosimp.nautilusserver.server.controllers;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.services.ServerService;

@WebMvcTest(ServerController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServerControllerWebTest {
    @MockitoBean ServerService serverService;
    @MockitoBean GlobalIdResolver globalIdResolver;
    @Autowired MockMvcTester mvc;

    @Test
    void listEndpointParsesFiltersAndPagination() {
        when(serverService.getAllServer(
                        eq(true), eq(ServerType.VANILLA), eq(ServerPlatform.JAVA), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(0, 10), 0));
        mvc.get()
                .uri(
                        "/servers?requiresPremium=true&serverType=VANILLA&serverPlatform=JAVA&page=0&size=10")
                .assertThat()
                .hasStatusOk();
        verify(serverService)
                .getAllServer(
                        eq(true), eq(ServerType.VANILLA), eq(ServerPlatform.JAVA), any(), any());
    }

    @Test
    void malformedEnumFilterReturnsBadRequest() {
        mvc.get().uri("/servers?serverType=UNKNOWN").assertThat().hasStatus4xxClientError();
        verifyNoInteractions(serverService);
    }
}
