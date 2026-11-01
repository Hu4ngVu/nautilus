package vn.io.huangnosimp.nautilusserver.star.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.io.huangnosimp.nautilusserver.star.dto.ServerStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.dto.UserStaredInfo;

public interface StarService {
    Page<UserStaredInfo> getUserStaredInfo(String serverId, Pageable pageable);

    Page<ServerStaredInfo> getServerStaredInfo(String userId, Pageable pageable);

    void addStar(String userId, String serverId);

    void deleteStar(String userId, String serverId);
}
