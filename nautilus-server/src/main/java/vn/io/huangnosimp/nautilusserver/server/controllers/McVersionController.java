package vn.io.huangnosimp.nautilusserver.server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddBedrockVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddJavaVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.BedrockVersionResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.JavaVersionResponse;
import vn.io.huangnosimp.nautilusserver.server.services.ServerMcVersionService;

@RestController
@RequestMapping("/servers/versions")
@RequiredArgsConstructor
public class McVersionController {
    private final ServerMcVersionService serverMcVersionService;

    @GetMapping("/java/{username}/{servername}")
    public ResponseEntity<JavaVersionResponse> getJavaVersions(
            @PathVariable String username, @PathVariable String servername) {
        JavaVersionResponse response =
                serverMcVersionService.getAllJavaVersion(username, servername);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bedrock/{username}/{servername}")
    public ResponseEntity<BedrockVersionResponse> getBedrockVersions(
            @PathVariable String username, @PathVariable String servername) {
        BedrockVersionResponse response =
                serverMcVersionService.getAllBedrockVersion(username, servername);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/java/{username}/{servername}")
    public ResponseEntity<?> addJavaVersion(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody AddJavaVersionRequest body) {
        serverMcVersionService.addJavaVersion(username, servername, body);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/bedrock/{username}/{servername}")
    public ResponseEntity<?> addBedrockVersion(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody AddBedrockVersionRequest body) {
        serverMcVersionService.addBedrockVersion(username, servername, body);
        return ResponseEntity.ok().build();
    }
}
