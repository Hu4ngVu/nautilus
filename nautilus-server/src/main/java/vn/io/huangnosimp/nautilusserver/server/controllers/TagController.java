package vn.io.huangnosimp.nautilusserver.server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.resolver.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddTagRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.TagResponse;
import vn.io.huangnosimp.nautilusserver.server.services.ServerTagService;

@RestController
@RequestMapping("/servers/tags")
@RequiredArgsConstructor
public class TagController {
    private final ServerTagService serverTagService;
    private final GlobalIdResolver globalIdResolver;

    @GetMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<TagResponse> getAllTags(
            @PathVariable String username, @PathVariable String servername) {
        TagResponse response = serverTagService.getAllServerTag(getServerId(username, servername));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/{servername}")
    @PreAuthorize(
            "@permissionCheckerImpl.isEditor(@globalIdResolver.resolveUserId(@authentication.getName()),"
                + " @globalIdResolver.resolveServerId(#username, #servername))")
    public ResponseEntity<?> addServerTag(
            @PathVariable String username,
            @PathVariable String servername,
            @Valid @RequestBody AddTagRequest body) {
        serverTagService.addServerTag(getServerId(username, servername), body);
        return ResponseEntity.ok().build();
    }

    private String getServerId(String username, String servername) {
        return globalIdResolver.resolveServerId(username, servername);
    }
}
