package vn.io.huangnosimp.nautilusserver.auth;

public interface AuthorizationUtils {
    boolean canEdit(String authenticationName, String username, String servername);

    boolean canView(String authenticationName, String username, String servername);

    boolean canDelete(String authenticationName, String username, String servername);
}
