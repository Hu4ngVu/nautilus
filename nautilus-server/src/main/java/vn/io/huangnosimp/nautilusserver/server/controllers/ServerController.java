package vn.io.huangnosimp.nautilusserver.server.controllers;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<ServerInfoDetailResponse> getServerInfo(
            @PathVariable String username, @PathVariable String servername) {
        return ResponseEntity.ok(serverService.getServerInfo(username, servername));
    }

    @PostMapping("/{username}")
    public ResponseEntity<?> createServer(
            @PathVariable String username, @Valid @RequestBody CreateServerRequest body) {
        serverService.createServer(username, body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}/{servername}")
    public ResponseEntity<?> deleteServer(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServer(username, servername);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{username}/{servername}")
    public ResponseEntity<?> updateServer(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody UpdateServerInfoRequest body) {
        serverService.updateServerInfo(username, servername, body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{username}/{servername}/avatar")
    public ResponseEntity<?> deleteServerAvatar(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServerAvatar(username, servername);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{username}/{servername}/banner")
    public ResponseEntity<?> deleteServerBanner(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServerBanner(username, servername);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{username}/{servername}")
    public ResponseEntity<?> changeServername(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestParam ChangeServernameRequest request) {
        serverService.changeServername(username, servername, request.newServername());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{username}/{servername}/visibility")
    public ResponseEntity<?> changeServerVisibility(
            @PathVariable String username,
            @PathVariable String servername,
            @RequestBody ChangeServerVisibility request) {
        serverService.changeServerVisibility(username, servername, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
