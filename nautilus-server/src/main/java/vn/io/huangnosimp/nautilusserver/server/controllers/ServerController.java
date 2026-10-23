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

    @GetMapping("/{leaderName}/{servername}")
    public ResponseEntity<ServerInfoResponse> getServerInfo(@PathVariable String leaderName, @PathVariable String servername) {
        return ResponseEntity.ok(serverService.getServerInfo(leaderName, servername));
    }

    @PostMapping("/{leaderName}")
    public ResponseEntity<?> createServer(@PathVariable String leaderName, @Valid @RequestBody CreateServerRequest body) {
        serverService.createServer(leaderName, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{leaderName}/{servername}")
    public ResponseEntity<?> deleteServer(@PathVariable String leaderName, @PathVariable String servername) {
        serverService.deleteServer(leaderName, servername);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{leaderName}/{servername}")
    public ResponseEntity<?> updateServer(@PathVariable String leaderName, @PathVariable String servername, @Valid @RequestBody UpdateServerInfoRequest body) {
        serverService.updateServerInfo(leaderName, servername, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{leaderName}/{servername}/avatar")
    public ResponseEntity<?> deleteServerAvatar(@PathVariable String leaderName, @PathVariable String servername) {
        serverService.deleteServerAvatar(leaderName, servername);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{leaderName}/{servername}/banner")
    public ResponseEntity<?> deleteServerBanner(@PathVariable String leaderName, @PathVariable String servername) {
        serverService.deleteServerBanner(leaderName, servername);
        return ResponseEntity.ok().build();
    }
}
