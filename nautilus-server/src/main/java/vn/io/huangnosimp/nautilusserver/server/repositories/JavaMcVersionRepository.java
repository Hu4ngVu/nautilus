package vn.io.huangnosimp.nautilusserver.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.huangnosimp.nautilusserver.server.models.JavaMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

import java.util.List;

public interface JavaMcVersionRepository extends JpaRepository<JavaMcVersion, String> {
    List<JavaMcVersion> findAllByServer(Server server);
}
