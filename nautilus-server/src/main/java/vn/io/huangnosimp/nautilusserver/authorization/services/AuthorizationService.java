package vn.io.huangnosimp.nautilusserver.authorization.services;

public interface AuthorizationService {
    void createOwnerRelation(String userId, String serverId);

    void deleteOwnerRelation(String userId, String serverId);

    void createPublicRelation(String serverId);

    void deletePublicRelation(String serverId);
}
