package vn.io.huangnosimp.nautilusserver.services.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.constants.Status;
import vn.io.huangnosimp.nautilusserver.repositories.InvitationRepository;
import vn.io.huangnosimp.nautilusserver.repositories.TransferRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeStatusServiceImpl implements  ChangeStatusService {
    private final InvitationRepository invitationRepository;
    private final TransferRepository transferRepository;

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void expireInvitation(String invitationId) {
        log.debug("Expiring invitation: {}", invitationId);
        invitationRepository.findById(invitationId).ifPresent(invitation -> {
            if (invitation.getStatus() == Status.PENDING) {
                invitation.setStatus(Status.EXPIRED);
                invitationRepository.save(invitation);
                log.info("Invitation {} expired successfully", invitationId);
            }
        });
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void expireTransfer(String transferId) {
        log.debug("Expiring transfer: {}", transferId);
        transferRepository.findById(transferId).ifPresent(transfer -> {
            if (transfer.getStatus() == Status.PENDING) {
                transfer.setStatus(Status.EXPIRED);
                transferRepository.save(transfer);
                log.info("Transfer {} expired successfully", transferId);
            }
        });
    }
}
