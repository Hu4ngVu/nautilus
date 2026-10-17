package vn.io.huangnosimp.nautilusserver.services;

public interface OwnerService {
    void createInvite(String leaderName, String servername, String username);
    void cancelInvite(String leaderName, String servername, String username);
    void acceptInvite(String leaderName, String servername, String username);
    void declineInvite(String leaderName, String servername, String username);
    void createTransfer(String leaderName, String ownerName, String servername);
    void cancelTransfer(String leaderName, String ownerName, String servername);
    void acceptTransfer(String leaderName, String ownerName, String servername);
    void declineTransfer(String leaderName, String ownerName, String servername);
}
