package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.dto.request.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.services.ServerService;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {
    private final ServerService serverService;

    @GetMapping("/{username}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isPublic(#username, #servername) or @serverAccessEvaluator.isOwner(authentication.name, #username, #servername)")
    public ResponseEntity<ServerInfoResponse> getServerInfo(@PathVariable String username, @PathVariable String servername) {
        return ResponseEntity.ok(serverService.getServerInfo(username, servername));
    }

    @PostMapping("/{username}")
    @PreAuthorize("@userAccessEvaluator.isOwner(authentication.name, #username)")
    public ResponseEntity<?> createServer(@PathVariable String username, @Valid @RequestBody CreateServerRequest body) {
        serverService.createServer(username, body);
        return ResponseEntity.ok("Server created for " + username);
    }

    @DeleteMapping("{username}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isOwner(authentication.name, #username, #servername)")
    public ResponseEntity<?> deleteServer(@PathVariable String username, @PathVariable String servername) {
        serverService.deleteServer(username, servername);
        return ResponseEntity.ok("Server deleted for " + username);
    }
}
