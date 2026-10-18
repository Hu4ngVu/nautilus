package vn.io.huangnosimp.nautilusserver.services.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.constants.Status;
import vn.io.huangnosimp.nautilusserver.repositories.InvitationRepository;
import vn.io.huangnosimp.nautilusserver.repositories.TransferRepository;

@Service
@RequiredArgsConstructor
public class ChangeStatusServiceImpl implements  ChangeStatusService {
    private final InvitationRepository invitationRepository;
    private final TransferRepository transferRepository;

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void expireInvitation(String invitationId) {
        invitationRepository.findById(invitationId).ifPresent(invitation -> {
            if (invitation.getStatus() == Status.PENDING) {
                invitation.setStatus(Status.EXPIRED);
                invitationRepository.save(invitation);
            }
        });
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void expireTransfer(String transferId) {
        transferRepository.findById(transferId).ifPresent(transfer -> {
            if (transfer.getStatus() == Status.PENDING) {
                transfer.setStatus(Status.EXPIRED);
                transferRepository.save(transfer);
            }
        });
    }
}
