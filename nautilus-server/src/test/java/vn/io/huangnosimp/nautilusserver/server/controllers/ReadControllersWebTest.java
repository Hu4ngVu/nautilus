package vn.io.huangnosimp.nautilusserver.server.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.authentication.controllers.UsernameEmailCheckingController;
import vn.io.huangnosimp.nautilusserver.authentication.services.UsernameEmailCheckingService;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.server.services.GalleryService;
import vn.io.huangnosimp.nautilusserver.server.services.ServerMcVersionService;
import vn.io.huangnosimp.nautilusserver.server.services.ServerTagService;
import vn.io.huangnosimp.nautilusserver.star.controllers.StarController;
import vn.io.huangnosimp.nautilusserver.star.services.StarService;

@WebMvcTest({
    UsernameEmailCheckingController.class,
    GalleryController.class,
    McVersionController.class,
    TagController.class,
    StarController.class
})
@AutoConfigureMockMvc(addFilters = false)
class ReadControllersWebTest {
    @MockitoBean UsernameEmailCheckingService checkingService;
    @MockitoBean GalleryService galleryService;
    @MockitoBean ServerMcVersionService versionService;
    @MockitoBean ServerTagService tagService;
    @MockitoBean StarService starService;
    @MockitoBean GlobalIdResolver globalIdResolver;
    @Autowired MockMvcTester mvc;

    @Test
    void usernameCheckReturnsServiceResult() {
        when(checkingService.isUsernameExists("alice")).thenReturn(true);
        mvc.post()
                .uri("/auth/check-username?username=alice")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(Boolean.class)
                .satisfies(result -> assertEquals(true, result));
    }

    @Test
    void galleryAndStarsReadRoutesResolveIdsAndReturnPages() {
        when(globalIdResolver.resolveServerId("owner", "server")).thenReturn("server-id");
        when(globalIdResolver.resolveUserId("alice")).thenReturn("user-id");
        when(galleryService.getAllGallery(eq("server-id"), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(0, 20), 0));
        when(starService.getServerStaredInfo(eq("user-id"), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(0, 20), 0));

        mvc.get().uri("/servers/gallery/owner/server").assertThat().hasStatusOk();
        mvc.get().uri("/stars/alice").assertThat().hasStatusOk();
        verify(galleryService).getAllGallery(eq("server-id"), any());
        verify(starService).getServerStaredInfo(eq("user-id"), any());
    }

    @Test
    void tagAndVersionReadRoutesResolveServerIdAndDelegate() {
        when(globalIdResolver.resolveServerId("owner", "server")).thenReturn("server-id");

        mvc.get().uri("/servers/tags/owner/server").assertThat().hasStatusOk();
        mvc.get().uri("/servers/versions/java/owner/server").assertThat().hasStatusOk();
        verify(tagService).getAllServerTag("server-id");
        verify(versionService).getAllJavaVersion("server-id");
    }
}
