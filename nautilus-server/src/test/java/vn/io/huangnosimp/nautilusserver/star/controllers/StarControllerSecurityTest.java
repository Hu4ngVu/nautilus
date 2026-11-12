package vn.io.huangnosimp.nautilusserver.star.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.api.PermissionChecker;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.star.services.StarService;

@WebMvcTest(StarController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(StarControllerSecurityTest.MethodSecurity.class)
@WithMockUser("bob")
class StarControllerSecurityTest {
    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurity {}

    @MockitoBean(name = "permissionCheckerImpl")
    PermissionChecker permissionChecker;

    @MockitoBean(name = "globalIdResolver")
    GlobalIdResolver globalIdResolver;

    @MockitoBean StarService starService;
    @Autowired MockMvcTester mvc;

    @BeforeEach
    void resolveIds() {
        when(globalIdResolver.resolveUserId("bob")).thenReturn("bob-id");
        when(globalIdResolver.resolveServerId("owner", "server")).thenReturn("server-id");
        when(starService.getUserStaredInfo(eq("server-id"), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(0, 20), 0));
    }

    @Test
    void viewerWhoIsNotEditorIsForbiddenFromStargazers() {
        when(permissionChecker.isViewer("bob-id", "server-id")).thenReturn(true);
        when(permissionChecker.isEditor("bob-id", "server-id")).thenReturn(false);

        // Filters are off in this slice; in the app ExceptionTranslationFilter maps this to 403
        mvc.get()
                .uri("/stars/owner/server")
                .assertThat()
                .failure()
                .hasRootCauseInstanceOf(AuthorizationDeniedException.class);
        verify(starService, never()).getUserStaredInfo(any(), any());
    }

    @Test
    void editorCanListStargazers() {
        when(permissionChecker.isEditor("bob-id", "server-id")).thenReturn(true);

        mvc.get().uri("/stars/owner/server").assertThat().hasStatusOk();
        verify(starService).getUserStaredInfo(eq("server-id"), any());
    }
}
