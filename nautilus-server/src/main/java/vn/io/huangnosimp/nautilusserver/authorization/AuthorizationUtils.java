package vn.io.huangnosimp.nautilusserver.authorization;

public interface AuthorizationUtils {
    boolean isEditor(String userId, String serverId);

    boolean isViewer(String userId, String serverId);

    boolean isOwner(String userId, String serverId);

    boolean isServerPublic(String serverId);
}
