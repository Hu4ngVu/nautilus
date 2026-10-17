package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.dto.request.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.services.ServerService;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {
    private final ServerService serverService;

    @GetMapping("/{leaderName}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isPublic(#leaderName, #servername) or @serverAccessEvaluator.isOwner(authentication.name, #leaderName, #servername)")
    public ResponseEntity<ServerInfoResponse> getServerInfo(@PathVariable String leaderName, @PathVariable String servername) {
        return ResponseEntity.ok(serverService.getServerInfo(leaderName, servername));
    }

    @PostMapping("/{leaderName}")
    @PreAuthorize("@userAccessEvaluator.isOwner(authentication.name, #leaderName)")
    public ResponseEntity<?> createServer(@PathVariable String leaderName, @Valid @RequestBody CreateServerRequest body) {
        serverService.createServer(leaderName, body);
        return ResponseEntity.ok("Server created for " + leaderName);
    }

    @DeleteMapping("{leaderName}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isLeader(authentication.name, #leaderName, #servername)")
    public ResponseEntity<?> deleteServer(@PathVariable String leaderName, @PathVariable String servername) {
        serverService.deleteServer(leaderName, servername);
        return ResponseEntity.ok("Server deleted for " + leaderName);
    }

    @PatchMapping("{leaderName}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isOwner(authentication.name, #leaderName, #servername)")
    public ResponseEntity<?> updateServer(@PathVariable String leaderName, @PathVariable String servername, @Valid @RequestBody UpdateServerInfoRequest body) {
        serverService.updateServerInfo(leaderName, servername, body);
        return ResponseEntity.ok("Server updated for " + leaderName);
    }
}
