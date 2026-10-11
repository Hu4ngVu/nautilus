package vn.io.huangnosimp.nautilusserver.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.io.huangnosimp.nautilusserver.services.interfaces.UsernameEmailCheckingService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UsernameEmailCheckingController {

    private final UsernameEmailCheckingService usernameEmailCheckingService;

    @PostMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(usernameEmailCheckingService.isUsernameExists(username));
    }

    @PostMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(usernameEmailCheckingService.isEmailExists(email));
    }
}
