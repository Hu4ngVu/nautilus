package vn.io.huangnosimp.nautilusserver.server.controllers;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.ChangeServerVisibility;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.ChangeServernameRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoDetailResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.server.services.ServerService;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {
    private final ServerService serverService;
    private final GlobalIdResolver globalIdResolver;

    @GetMapping
    public ResponseEntity<Page<ServerInfoResponse>> getServer(
            @RequestParam(required = false) Boolean requiresPremium,
            @RequestParam(required = false) ServerType serverType,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) ServerPlatform serverPlatform,
            @PageableDefault(sort = "starCount", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(
                serverService.getAllServer(
                        requiresPremium, serverType, serverPlatform, tags, pageable));
    }

    @GetMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isServerPublic(@globalIdResolver.resolveServerId(#username,"
                + " #servername)) || "
                + "@permissionCheckerImpl.isViewer(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<ServerInfoDetailResponse> getServerInfo(
            @PathVariable String username, @PathVariable String servername) {
        return ResponseEntity.ok(serverService.getServerInfo(getServerId(username, servername)));
    }

    @PostMapping("/{username}")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<?> createServer(
            @PathVariable String username, @Valid @RequestBody CreateServerRequest body) {
        serverService.createServer(getUserId(username), body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isOwner(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> deleteServer(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServer(getServerId(username, servername));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> updateServer(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody UpdateServerInfoRequest body) {
        serverService.updateServerInfo(getServerId(username, servername), body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{username}/{servername}/avatar")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> deleteServerAvatar(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServerAvatar(getServerId(username, servername));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{username}/{servername}/banner")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> deleteServerBanner(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServerBanner(getServerId(username, servername));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{username}/{servername}")
    @CacheEvict(cacheNames = CacheName.SERVER_ID_CACHE, key = "#username + ':' + #servername")
    @PreAuthorize(
            "@permissionCheckerImpl.isOwner(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> changeServername(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestParam ChangeServernameRequest request) {
        serverService.changeServername(getServerId(username, servername), request.newServername());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{username}/{servername}/visibility")
    @PreAuthorize(
            "@permissionCheckerImpl.isOwner(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> changeServerVisibility(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody ChangeServerVisibility request) {
        serverService.changeServerVisibility(getServerId(username, servername), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private String getServerId(String username, String servername) {
        return globalIdResolver.resolveServerId(username, servername);
    }

    private String getUserId(String username) {
        return globalIdResolver.resolveUserId(username);
    }
}
