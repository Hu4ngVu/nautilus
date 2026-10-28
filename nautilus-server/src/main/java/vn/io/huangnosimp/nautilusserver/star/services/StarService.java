package vn.io.huangnosimp.nautilusserver.star.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.io.huangnosimp.nautilusserver.star.dto.ServerStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.dto.UserStaredInfo;

public interface StarService {
    Page<UserStaredInfo> getUserStaredInfo(String username, String servername, Pageable pageable);

    Page<ServerStaredInfo> getServerStaredInfo(String username, Pageable pageable);

    void addStar(String authenticationName, String username, String servername);

    void deleteStar(String authenticationName, String username, String servername);
}
