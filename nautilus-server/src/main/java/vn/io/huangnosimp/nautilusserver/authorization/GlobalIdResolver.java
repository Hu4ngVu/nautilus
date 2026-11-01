package vn.io.huangnosimp.nautilusserver.authorization;

public interface GlobalIdResolver {
    String resolveUserId(String username);

    String resolveServerId(String username, String servername);
}
