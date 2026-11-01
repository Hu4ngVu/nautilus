package vn.io.huangnosimp.nautilusserver.user.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.authorization.GlobalIdResolver;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.services.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final GlobalIdResolver globalIdResolver;

    @GetMapping("/{username}")
    public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserInfo(getId(username)));
    }

    @PatchMapping("/{username}")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable String username, @Valid @RequestBody UpdateUserInfoRequest body) {
        userService.updateUserInfo(getId(username), body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/avatar")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<?> deleteUserAvatar(@PathVariable String username) {
        userService.deleteUserAvatar(getId(username));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<?> changeUsername(
            @PathVariable String username, @RequestBody ChangeUsernameRequest request) {
        userService.changeUsername(getId(username), request);
        return ResponseEntity.ok().build();
    }

    private String getId(String username) {
        return globalIdResolver.resolveUserId(username);
    }
}
