package vn.io.huangnosimp.nautilusserver.user.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.services.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{username}")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserInfo(username));
    }

    @PatchMapping("/{username}")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable String username, @Valid @RequestBody UpdateUserInfoRequest body) {
        userService.updateUserInfo(username, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/avatar")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<?> deleteUserAvatar(@PathVariable String username) {
        userService.deleteUserAvatar(username);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}")
    @PreAuthorize("authentication.name == #username")
    public ResponseEntity<?> changeUsername(
            @PathVariable String username, @RequestBody ChangeUsernameRequest request) {
        userService.changeUsername(username, request);
        return ResponseEntity.ok().build();
    }
}
