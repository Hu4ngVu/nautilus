package vn.io.huangnosimp.nautilusserver.api;

public interface ResourceStorage {
    String getPublicUrl(String resourceId);

    boolean isResourceIdExist(String resourceId);
}
