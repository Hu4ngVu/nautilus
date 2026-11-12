package vn.io.huangnosimp.nautilusserver.star.controllers;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.star.dto.ServerStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.dto.UserStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.services.StarService;

@RestController
@RequestMapping("/stars")
@RequiredArgsConstructor
public class StarController {
    private final StarService starService;
    private final GlobalIdResolver globalIdResolver;

    @GetMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isViewer(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<Page<UserStaredInfo>> getUserStaredInfo(
            @PathVariable String username,
            @PathVariable String servername,
            @PageableDefault(
                            size = 20,
                            sort = {"createdAt"})
                    Pageable pageable) {
        String serverId = globalIdResolver.resolveServerId(username, servername);
        return ResponseEntity.ok(starService.getUserStaredInfo(serverId, pageable));
    }

    @GetMapping("/{username}")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<Page<ServerStaredInfo>> getServerStaredInfo(
            @PathVariable String username,
            @PageableDefault(
                            size = 20,
                            sort = {"createdAt"})
                    Pageable pageable) {
        String userId = globalIdResolver.resolveUserId(username);
        return ResponseEntity.ok(starService.getServerStaredInfo(userId, pageable));
    }

    @PutMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isViewer(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> addStar(
            @PathVariable String username, @PathVariable String servername) {
        String userId = globalIdResolver.resolveUserId(getAuthentication().getName());
        String serverId = globalIdResolver.resolveServerId(username, servername);
        starService.addStar(userId, serverId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isViewer(@globalIdResolver.resolveUserId(authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> deleteStar(
            @PathVariable String username, @PathVariable String servername) {
        String userId = globalIdResolver.resolveUserId(getAuthentication().getName());
        String serverId = globalIdResolver.resolveServerId(username, servername);
        starService.deleteStar(userId, serverId);
        return ResponseEntity.noContent().build();
    }

    private Authentication getAuthentication() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
