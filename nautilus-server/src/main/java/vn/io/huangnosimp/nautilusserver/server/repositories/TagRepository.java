package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;

@Repository
public interface TagRepository extends JpaRepository<ServerTag, String> {
    List<ServerTag> findAllByServer(Server server);

    void deleteAllByServerId(String serverId);

    List<ServerTag> findAllByServerIdInOrderByCreatedAtAsc(Collection<String> serverIds);
}
