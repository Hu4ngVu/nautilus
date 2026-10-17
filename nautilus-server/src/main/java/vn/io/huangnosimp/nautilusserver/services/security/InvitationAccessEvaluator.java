package vn.io.huangnosimp.nautilusserver.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.repositories.InvitationRepository;

@Service
@RequiredArgsConstructor
public class InvitationAccessEvaluator {
    private final InvitationRepository invitationRepository;

    public boolean isSender(String authenticationName, String senderName, String receiverName, String servername) {
        return authenticationName.equals(senderName) && invitationRepository.existsPendingInvitation(senderName, receiverName, servername);
    }

    public boolean isReceiver(String authenticationName, String senderName, String receiverName, String servername) {
        return authenticationName.equals(receiverName) && invitationRepository.existsPendingInvitation(senderName, receiverName, servername);
    }
}
