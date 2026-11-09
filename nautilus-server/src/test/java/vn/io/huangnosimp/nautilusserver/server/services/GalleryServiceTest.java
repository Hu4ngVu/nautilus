package vn.io.huangnosimp.nautilusserver.server.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.ApplicationEventPublisher;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.events.ImageDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.ImageUploadedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.ImageNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.ResourceNotFound;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateImageInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UploadImageRequest;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.GalleryRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {
    @Mock GalleryRepository gallery;
    @Mock ServerRepository servers;
    @Mock ResourceStorage storage;
    @Mock ApplicationEventPublisher events;
    @InjectMocks GalleryService service;

    @Test
    void uploadUsesDefaultsAndPublishesResourceEvent() {
        Server server = Server.builder().build();
        when(servers.findById("server")).thenReturn(Optional.of(server));
        service.uploadImage(
                "server",
                new UploadImageRequest(
                        "resource",
                        JsonNullable.undefined(),
                        JsonNullable.undefined(),
                        JsonNullable.undefined()));

        verify(gallery)
                .save(
                        argThat(
                                image ->
                                        image.getServer() == server
                                                && image.getImageId().equals("resource")
                                                && image.getOrderIndex() == 0));
        verify(events).publishEvent(new ImageUploadedEvent("resource"));
    }

    @Test
    void updateRejectsMissingResourceAndReplacesExistingImageWithEvents() {
        Server server = Server.builder().build();
        Image image = Image.builder().server(server).imageId("old").build();
        when(servers.findById("server")).thenReturn(Optional.of(server));
        when(gallery.findByServerAndId(server, "image")).thenReturn(Optional.of(image));
        when(storage.isResourceIdExist("missing")).thenReturn(false);
        assertThrows(
                ResourceNotFound.class,
                () ->
                        service.updateImageInfo(
                                "server",
                                "image",
                                new UpdateImageInfoRequest(
                                        "missing",
                                        JsonNullable.undefined(),
                                        JsonNullable.undefined(),
                                        JsonNullable.undefined())));
        verify(gallery, never()).save(any());

        when(storage.isResourceIdExist("new")).thenReturn(true);
        service.updateImageInfo(
                "server",
                "image",
                new UpdateImageInfoRequest(
                        "new",
                        JsonNullable.of("title"),
                        JsonNullable.undefined(),
                        JsonNullable.of(4)));
        assertEquals("new", image.getImageId());
        assertEquals("title", image.getTitle());
        assertEquals(4, image.getOrderIndex());
        verify(events).publishEvent(new ImageDeletedEvent("old"));
        verify(events).publishEvent(new ImageUploadedEvent("new"));
        verify(gallery).save(image);
    }

    @Test
    void deleteImageRejectsUnknownImageAndEmitsDeletionForStoredResource() {
        Server server = Server.builder().build();
        Image image = Image.builder().server(server).imageId("resource").build();
        when(servers.findById("server")).thenReturn(Optional.of(server));
        when(gallery.findByServerAndId(server, "missing")).thenReturn(Optional.empty());
        assertThrows(ImageNotFound.class, () -> service.deleteImage("server", "missing"));

        when(gallery.findByServerAndId(server, "image")).thenReturn(Optional.of(image));
        service.deleteImage("server", "image");
        verify(gallery).delete(image);
        verify(events).publishEvent(new ImageDeletedEvent("resource"));
    }
}
