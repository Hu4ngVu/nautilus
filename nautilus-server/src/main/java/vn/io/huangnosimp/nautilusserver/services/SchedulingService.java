package vn.io.huangnosimp.nautilusserver.services;

import vn.io.huangnosimp.nautilusserver.models.Invitation;
import vn.io.huangnosimp.nautilusserver.models.Transfer;

public interface SchedulingService {
    void scheduleInvitationExpiration(Invitation invitation);
    void cancelInvitationExpiration(String invitationId);

    void scheduleTransferExpiration(Transfer transfer);
    void cancelTransferExpiration(String transferId);
}
