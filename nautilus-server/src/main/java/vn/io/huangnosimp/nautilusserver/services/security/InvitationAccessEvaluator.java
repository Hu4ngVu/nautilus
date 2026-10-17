package vn.io.huangnosimp.nautilusserver.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.repositories.ServerInvitationRepository;

@Service
@RequiredArgsConstructor
public class InvitationAccessEvaluator {
    private final ServerInvitationRepository serverInvitationRepository;

    public boolean isSender(String authenticationName, String senderName, String receiverName, String servername) {
        return authenticationName.equals(senderName) && serverInvitationRepository.existsPendingInvitation(senderName, receiverName, servername);
    }

    public boolean isReceiver(String authenticationName, String senderName, String receiverName, String servername) {
        return authenticationName.equals(receiverName) && serverInvitationRepository.existsPendingInvitation(senderName, receiverName, servername);
    }
}
