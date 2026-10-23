package vn.io.huangnosimp.nautilusserver.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<ServerTag, String> {
    List<ServerTag> findAllByServer(Server server);
    void deleteAllByServerId(String serverId);
}
