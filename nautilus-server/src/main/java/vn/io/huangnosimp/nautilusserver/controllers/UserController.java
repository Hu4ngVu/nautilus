package vn.io.huangnosimp.nautilusserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.services.UserService;

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
    @PreAuthorize("@userAccessEvaluator.isOwner(authentication.name, #username)")
    public ResponseEntity<?> updateUserInfo(@PathVariable String username, @Valid @ModelAttribute UpdateUserInfoRequest body) {
        userService.updateUserInfo(username, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/avatar")
    @PreAuthorize("@userAccessEvaluator.isOwner(authentication.name, #username)")
    public ResponseEntity<?> deleteUserAvatar(@PathVariable String username) {
        userService.deleteUserAvatar(username);
        return ResponseEntity.ok().build();
    }
}
