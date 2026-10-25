package vn.io.huangnosimp.nautilusserver.user.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.services.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserInfo(username));
    }

    @PatchMapping("/{username}")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable String username, @Valid @ModelAttribute UpdateUserInfoRequest body) {
        userService.updateUserInfo(username, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/avatar")
    public ResponseEntity<?> deleteUserAvatar(@PathVariable String username) {
        userService.deleteUserAvatar(username);
        return ResponseEntity.ok().build();
    }
}
