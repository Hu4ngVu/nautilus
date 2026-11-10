package vn.io.huangnosimp.nautilusserver.storage.controllers;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import vn.io.huangnosimp.nautilusserver.storage.dto.responses.UploadUrlResponse;
import vn.io.huangnosimp.nautilusserver.storage.services.StorageService;

@WebMvcTest(StorageController.class)
@AutoConfigureMockMvc(addFilters = false)
class StorageControllerWebTest {
    @MockitoBean StorageService storageService;
    @Autowired MockMvcTester mvc;

    @Test
    void uploadUrlPassesContentMetadataToService() {
        when(storageService.generateUploadUrl("image/png", 128L))
                .thenReturn(new UploadUrlResponse("https://example.test/upload", "resource-id"));
        mvc.get()
                .uri("/storage/upload-url?contentType=image/png&contentLength=128")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(UploadUrlResponse.class)
                .satisfies(
                        response -> {
                            org.junit.jupiter.api.Assertions.assertEquals(
                                    "resource-id", response.resourceId());
                            org.junit.jupiter.api.Assertions.assertEquals(
                                    "https://example.test/upload", response.uploadUrl());
                        });
        verify(storageService).generateUploadUrl("image/png", 128L);
    }

    @Test
    void webhookRequiresSecretHeaderBeforeDelegating() {
        mvc.put()
                .uri("/storage/webhook/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resourceId\":\"resource-id\"}")
                .assertThat()
                .hasStatus4xxClientError();
        verifyNoInteractions(storageService);
    }
}
