package vn.io.huangnosimp.nautilusserver.star.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.api.UserQuery;
import vn.io.huangnosimp.nautilusserver.events.AddStarEvent;
import vn.io.huangnosimp.nautilusserver.events.DeleteStarEvent;
import vn.io.huangnosimp.nautilusserver.events.ServerData;
import vn.io.huangnosimp.nautilusserver.events.UserData;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.StarNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.star.models.Star;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;

@ExtendWith(MockitoExtension.class)
class StarServiceTest {
    @Mock UserQuery userQuery;
    @Mock ServerQuery serverQuery;
    @Mock StarRepository starRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks StarService starService;

    @Test
    void addingStarPersistsRelationAndPublishesCounterEvent() {
        starService.addStar("user-1", "server-1");

        verify(starRepository)
                .save(
                        argThat(
                                star ->
                                        star.getUserId().equals("user-1")
                                                && star.getServerId().equals("server-1")));
        verify(eventPublisher).publishEvent(new AddStarEvent("server-1"));
    }

    @Test
    void deletingStarRejectsMissingRelationAndPublishesEventAfterDelete() {
        when(starRepository.findByUserIdAndServerId("user-1", "server-1"))
                .thenReturn(Optional.empty());
        assertThrows(StarNotFound.class, () -> starService.deleteStar("user-1", "server-1"));
        verify(eventPublisher, never()).publishEvent(any());

        Star star = Star.builder().userId("user-1").serverId("server-1").build();
        when(starRepository.findByUserIdAndServerId("user-1", "server-1"))
                .thenReturn(Optional.of(star));
        starService.deleteStar("user-1", "server-1");
        verify(starRepository).delete(star);
        verify(eventPublisher).publishEvent(new DeleteStarEvent("server-1"));
    }

    @Test
    void starredUsersAreMappedAndMissingUserDataFailsClearly() {
        var pageable = PageRequest.of(0, 10);
        Star star = Star.builder().userId("user-1").serverId("server-1").build();
        when(starRepository.findAllByServerId("server-1", pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(star)));
        when(userQuery.getUserDataByAuthUserId("user-1"))
                .thenReturn(
                        Optional.of(UserData.builder().username("alice").avatarUrl("url").build()));

        var result = starService.getUserStaredInfo("server-1", pageable);
        assertEquals("alice", result.getContent().getFirst().username());
        assertEquals("url", result.getContent().getFirst().avatarUrl());

        when(userQuery.getUserDataByAuthUserId("user-1")).thenReturn(Optional.empty());
        assertThrows(UserNotFound.class, () -> starService.getUserStaredInfo("server-1", pageable));
    }

    @Test
    void starredServersRequirePublicServerAndMapServerAndUserData() {
        var pageable = PageRequest.of(0, 10);
        Star star = Star.builder().userId("user-1").serverId("server-1").build();
        when(starRepository.findAllByUserId("user-1", pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(star)));
        when(userQuery.getUserDataByAuthUserId("user-1"))
                .thenReturn(Optional.of(UserData.builder().username("alice").build()));
        when(serverQuery.getPublicServerDataByServerId("server-1"))
                .thenReturn(
                        Optional.of(
                                ServerData.builder()
                                        .servername("server")
                                        .serverAvatarUrl("avatar")
                                        .serverTotalStar(4)
                                        .build()));

        var result = starService.getServerStaredInfo("user-1", pageable).getContent().getFirst();
        assertEquals("alice", result.username());
        assertEquals("server", result.servername());
        assertEquals("avatar", result.serverAvatarUrl());
        assertEquals(4, result.serverTotalStar());

        when(serverQuery.getPublicServerDataByServerId("server-1")).thenReturn(Optional.empty());
        assertThrows(
                ServerNotFound.class, () -> starService.getServerStaredInfo("user-1", pageable));
    }
}
