package vn.io.huangnosimp.nautilusserver.api;

public interface ResourceStorage {
    String createSignedGetUrl(String resourceId);

    boolean isResourceIdExist(String resourceId);
}
