package vn.io.huangnosimp.nautilusserver.services;

public interface OwnerService {
    void sendInvite(String leaderName, String servername, String username);
    void cancelInvite(String leaderName, String servername, String username);
    void acceptInvite(String leaderName, String servername, String username);
    void declineInvite(String leaderName, String servername, String username);
}
