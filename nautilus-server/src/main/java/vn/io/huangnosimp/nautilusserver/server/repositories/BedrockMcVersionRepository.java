package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.huangnosimp.nautilusserver.server.models.BedrockMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

public interface BedrockMcVersionRepository extends JpaRepository<BedrockMcVersion, String> {
    List<BedrockMcVersion> findAllByServer(Server server);

    List<BedrockMcVersion> findAllByServerIdInAndIsPrimaryMcVersionTrue(
            Collection<String> serverIds);
}
