package vn.io.huangnosimp.nautilusserver.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.services.OwnerService;

@RestController
@RequestMapping("/Owner")
@RequiredArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;

    @PostMapping("/invite/{receiverName}/{leaderName}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isLeader(authentication.name, #servername)")
    public ResponseEntity<?> createInvite(@PathVariable String receiverName, @PathVariable String leaderName, @PathVariable String servername) {
        ownerService.sendInvite(leaderName, servername, receiverName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invite/{receiverName}/{leaderName}/{servername}")
    @PreAuthorize("@serverAccessEvaluator.isLeader(authentication.name, #servername) and @invitationAccessEvaluator.isSender(authentication.name, #leaderName, #receiverName, #servername)")
    public ResponseEntity<?> cancelInvite(@PathVariable String receiverName, @PathVariable String leaderName, @PathVariable String servername) {
        ownerService.cancelInvite(leaderName, servername, receiverName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/invite/{receiverName}/{leaderName}/{servername}/accept")
    @PreAuthorize("@invitationAccessEvaluator.isReceiver(authentication.name, #leaderName, #receiverName, #servername)")
    public ResponseEntity<?> acceptInvite(@PathVariable String receiverName, @PathVariable String leaderName, @PathVariable String servername) {
        ownerService.acceptInvite(leaderName, servername, receiverName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/invite/{receiverName}/{leaderName}/{servername}/decline")
    @PreAuthorize("@invitationAccessEvaluator.isReceiver(authentication.name, #leaderName, #receiverName, #servername)")
    public ResponseEntity<?> declineInvite(@PathVariable String receiverName, @PathVariable String leaderName, @PathVariable String servername) {
        ownerService.declineInvite(leaderName, servername, receiverName);
        return ResponseEntity.ok().build();
    }
}