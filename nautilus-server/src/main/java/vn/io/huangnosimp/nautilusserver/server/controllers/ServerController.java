package vn.io.huangnosimp.nautilusserver.server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.server.services.ServerService;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {
    private final ServerService serverService;

    @GetMapping("/{username}/{servername}")
    public ResponseEntity<ServerInfoResponse> getServerInfo(
            @PathVariable String username, @PathVariable String servername) {
        return ResponseEntity.ok(serverService.getServerInfo(username, servername));
    }

    @PostMapping("/{username}")
    public ResponseEntity<?> createServer(
            @PathVariable String username, @Valid @RequestBody CreateServerRequest body) {
        serverService.createServer(username, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/{servername}")
    public ResponseEntity<?> deleteServer(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServer(username, servername);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/{servername}")
    public ResponseEntity<?> updateServer(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody UpdateServerInfoRequest body) {
        serverService.updateServerInfo(username, servername, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/{servername}/avatar")
    public ResponseEntity<?> deleteServerAvatar(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServerAvatar(username, servername);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/{servername}/banner")
    public ResponseEntity<?> deleteServerBanner(
            @PathVariable String username, @PathVariable String servername) {
        serverService.deleteServerBanner(username, servername);
        return ResponseEntity.ok().build();
    }
}
