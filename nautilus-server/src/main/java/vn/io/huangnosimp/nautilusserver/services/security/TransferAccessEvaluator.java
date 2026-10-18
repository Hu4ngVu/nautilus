package vn.io.huangnosimp.nautilusserver.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.repositories.TransferRepository;

@Service
@RequiredArgsConstructor
public class TransferAccessEvaluator {
    private final TransferRepository transferRepository;

    public boolean isSender(String authenticationName, String senderName, String receiverName, String servername) {
        return authenticationName.equals(senderName) && transferRepository.existsPendingTransferWithSenderName(senderName, receiverName, servername);
    }

    public boolean isReceiver(String authenticationName, String senderName, String receiverName, String servername) {
        return authenticationName.equals(receiverName) && transferRepository.existsPendingTransferWithReceiverName(senderName, receiverName, servername);
    }
}
