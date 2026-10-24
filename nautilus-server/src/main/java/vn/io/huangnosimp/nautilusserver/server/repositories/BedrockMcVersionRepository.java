package vn.io.huangnosimp.nautilusserver.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.huangnosimp.nautilusserver.server.models.BedrockMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

import java.util.List;

public interface BedrockMcVersionRepository extends JpaRepository<BedrockMcVersion, String> {
    List<BedrockMcVersion> findAllByServer(Server server);
}
