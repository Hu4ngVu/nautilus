package vn.io.huangnosimp.nautilusserver.star.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import vn.io.huangnosimp.nautilusserver.api.AuthUserQuery;
import vn.io.huangnosimp.nautilusserver.api.ServerQuery;
import vn.io.huangnosimp.nautilusserver.api.UserQuery;
import vn.io.huangnosimp.nautilusserver.events.AddStarEvent;
import vn.io.huangnosimp.nautilusserver.events.DeleteStarEvent;
import vn.io.huangnosimp.nautilusserver.events.ServerData;
import vn.io.huangnosimp.nautilusserver.events.UserData;
import vn.io.huangnosimp.nautilusserver.exceptions.StarNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.star.dto.ServerStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.models.Star;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;

@ExtendWith(MockitoExtension.class)
class StarServiceTest {
    @Mock UserQuery userQuery;
    @Mock ServerQuery serverQuery;
    @Mock AuthUserQuery authUserQuery;
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

    private static final PageRequest PAGE = PageRequest.of(0, 10);

    private static ServerData serverData(String id, String owner, String name) {
        return ServerData.builder()
                .serverId(id)
                .ownerUserId(owner)
                .servername(name)
                .serverAvatarUrl("avatar-" + id)
                .serverTotalStar(4)
                .build();
    }

    private static Star star(String userId, String serverId) {
        return Star.builder().userId(userId).serverId(serverId).build();
    }

    @Test
    void starredServersReportOwnerUsernameNotStargazer() {
        when(starRepository.findServerIdsByUserId("fan")).thenReturn(List.of("s1"));
        when(serverQuery.filterPublicServerIds(List.of("s1"))).thenReturn(List.of("s1"));
        when(starRepository.findAllByUserIdAndServerIdIn("fan", List.of("s1"), PAGE))
                .thenReturn(new PageImpl<>(List.of(star("fan", "s1"))));
        when(serverQuery.getPublicServerDataByServerIds(List.of("s1")))
                .thenReturn(Map.of("s1", serverData("s1", "o1", "Atlantis")));
        when(authUserQuery.getUsernamesByAuthUserIds(Set.of("o1")))
                .thenReturn(Map.of("o1", "alice"));

        var result = starService.getServerStaredInfo("fan", PAGE).getContent().getFirst();
        assertEquals("alice", result.username());
        assertEquals("Atlantis", result.servername());
        assertEquals("avatar-s1", result.serverAvatarUrl());
        assertEquals(4, result.serverTotalStar());
        verify(userQuery, never()).getUserDataByAuthUserId(any());
    }

    @Test
    void privateStarredServerIsExcludedAndPaginationCountsOnlyPublic() {
        when(starRepository.findServerIdsByUserId("fan")).thenReturn(List.of("s1", "s2"));
        when(serverQuery.filterPublicServerIds(List.of("s1", "s2"))).thenReturn(List.of("s1"));
        when(starRepository.findAllByUserIdAndServerIdIn(eq("fan"), eq(List.of("s1")), any()))
                .thenReturn(new PageImpl<>(List.of(star("fan", "s1")), PAGE, 1));
        when(serverQuery.getPublicServerDataByServerIds(any()))
                .thenReturn(Map.of("s1", serverData("s1", "o1", "Atlantis")));
        when(authUserQuery.getUsernamesByAuthUserIds(any())).thenReturn(Map.of("o1", "alice"));

        var page = assertDoesNotThrow(() -> starService.getServerStaredInfo("fan", PAGE));
        assertEquals(1, page.getTotalElements());
        assertEquals("Atlantis", page.getContent().getFirst().servername());
    }

    @Test
    void serverTurnedPrivateMidRequestIsSkippedInsteadOfFailing() {
        when(starRepository.findServerIdsByUserId("fan")).thenReturn(List.of("s1", "s2"));
        when(serverQuery.filterPublicServerIds(any())).thenReturn(List.of("s1", "s2"));
        when(starRepository.findAllByUserIdAndServerIdIn(eq("fan"), any(), any()))
                .thenReturn(new PageImpl<>(List.of(star("fan", "s1"), star("fan", "s2"))));
        when(serverQuery.getPublicServerDataByServerIds(any()))
                .thenReturn(Map.of("s1", serverData("s1", "o1", "Atlantis")));
        when(authUserQuery.getUsernamesByAuthUserIds(any())).thenReturn(Map.of("o1", "alice"));

        var content = starService.getServerStaredInfo("fan", PAGE).getContent();
        assertEquals(
                List.of("Atlantis"), content.stream().map(ServerStaredInfo::servername).toList());
    }

    @Test
    void noPublicStarredServersReturnsEmptyPageWithoutPageQuery() {
        when(starRepository.findServerIdsByUserId("fan")).thenReturn(List.of("s1"));
        when(serverQuery.filterPublicServerIds(any())).thenReturn(List.of());

        assertTrue(starService.getServerStaredInfo("fan", PAGE).isEmpty());
        verify(starRepository, never()).findAllByUserIdAndServerIdIn(any(), any(), any());
        verify(serverQuery, never()).getPublicServerDataByServerIds(any());
    }

    @Test
    void starredListsResolveDataInOneBatchEach() {
        when(starRepository.findServerIdsByUserId("fan")).thenReturn(List.of("s1", "s2", "s3"));
        when(serverQuery.filterPublicServerIds(any())).thenReturn(List.of("s1", "s2", "s3"));
        when(starRepository.findAllByUserIdAndServerIdIn(eq("fan"), any(), any()))
                .thenReturn(
                        new PageImpl<>(
                                List.of(star("fan", "s1"), star("fan", "s2"), star("fan", "s3"))));
        when(serverQuery.getPublicServerDataByServerIds(any()))
                .thenReturn(
                        Map.of(
                                "s1", serverData("s1", "o1", "A"),
                                "s2", serverData("s2", "o2", "B"),
                                "s3", serverData("s3", "o1", "C")));
        when(authUserQuery.getUsernamesByAuthUserIds(any()))
                .thenReturn(Map.of("o1", "alice", "o2", "bob"));
        starService.getServerStaredInfo("fan", PAGE);
        verify(serverQuery, times(1)).getPublicServerDataByServerIds(any());
        verify(authUserQuery, times(1)).getUsernamesByAuthUserIds(any());
        verify(serverQuery, never()).getPublicServerDataByServerId(any());

        when(starRepository.findAllByServerId("s1", PAGE))
                .thenReturn(new PageImpl<>(List.of(star("u1", "s1"), star("u2", "s1"))));
        when(userQuery.getUserDataByAuthUserIds(any()))
                .thenReturn(
                        Map.of(
                                "u1",
                                        UserData.builder()
                                                .authUserId("u1")
                                                .username("alice")
                                                .avatarUrl("a")
                                                .build(),
                                "u2", UserData.builder().authUserId("u2").username("bob").build()));
        var stargazers = starService.getUserStaredInfo("s1", PAGE).getContent();
        assertEquals("alice", stargazers.get(0).username());
        assertEquals("a", stargazers.get(0).avatarUrl());
        assertEquals("bob", stargazers.get(1).username());
        verify(userQuery, times(1)).getUserDataByAuthUserIds(any());
        verify(userQuery, never()).getUserDataByAuthUserId(any());
    }

    @Test
    void stargazerWithMissingUserDataFailsClearly() {
        when(starRepository.findAllByServerId("s1", PAGE))
                .thenReturn(new PageImpl<>(List.of(star("u1", "s1"))));
        when(userQuery.getUserDataByAuthUserIds(any())).thenReturn(Map.of());
        assertThrows(UserNotFound.class, () -> starService.getUserStaredInfo("s1", PAGE));
    }
}
