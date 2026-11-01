package vn.io.huangnosimp.nautilusserver.server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.authorization.GlobalIdResolver;
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
    private final GlobalIdResolver globalIdResolver;

    @GetMapping("/java/{username}/{servername}")
    @PreAuthorize(
            "@authorizationUtilsImpl.isViewer(@globalIdResolverImpl.resolveUserId(@authentication.getName()),"
                + " @globalIdResolverImpl.resolveServerId(#username, #servername))")
    public ResponseEntity<JavaVersionResponse> getJavaVersions(
            @PathVariable String username, @PathVariable String servername) {
        JavaVersionResponse response =
                serverMcVersionService.getAllJavaVersion(getServerId(username, servername));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bedrock/{username}/{servername}")
    @PreAuthorize(
            "@authorizationUtilsImpl.isViewer(@globalIdResolverImpl.resolveUserId(@authentication.getName()),"
                + " @globalIdResolverImpl.resolveServerId(#username, #servername))")
    public ResponseEntity<BedrockVersionResponse> getBedrockVersions(
            @PathVariable String username, @PathVariable String servername) {
        BedrockVersionResponse response =
                serverMcVersionService.getAllBedrockVersion(getServerId(username, servername));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/java/{username}/{servername}")
    @PreAuthorize(
            "@authorizationUtilsImpl.isEditor(@globalIdResolverImpl.resolveUserId(@authentication.getName()),"
                + " @globalIdResolverImpl.resolveServerId(#username, #servername))")
    public ResponseEntity<?> addJavaVersion(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody AddJavaVersionRequest body) {
        serverMcVersionService.addJavaVersion(getServerId(username, servername), body);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/bedrock/{username}/{servername}")
    @PreAuthorize(
            "@authorizationUtilsImpl.isEditor(@globalIdResolverImpl.resolveUserId(@authentication.getName()),"
                + " @globalIdResolverImpl.resolveServerId(#username, #servername))")
    public ResponseEntity<?> addBedrockVersion(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody AddBedrockVersionRequest body) {
        serverMcVersionService.addBedrockVersion(getServerId(username, servername), body);
        return ResponseEntity.ok().build();
    }

    private String getServerId(String username, String servername) {
        return globalIdResolver.resolveServerId(username, servername);
    }
}
