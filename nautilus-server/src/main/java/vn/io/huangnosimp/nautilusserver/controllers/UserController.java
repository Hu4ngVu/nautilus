package vn.io.huangnosimp.nautilusserver.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @GetMapping("/{username}")
    public ResponseEntity<?> getPrivateUserInfo(@PathVariable String username) {
        return ResponseEntity.ok("User info for " + username);
    }


}
