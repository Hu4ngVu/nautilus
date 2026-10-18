package vn.io.huangnosimp.nautilusserver.services.task;

public interface ChangeStatusService {
    void expireInvitation(String invitationId);
    void expireTransfer(String transferId);
}
