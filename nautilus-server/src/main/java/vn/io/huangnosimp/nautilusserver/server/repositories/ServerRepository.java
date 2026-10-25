package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

@Repository
public interface ServerRepository extends JpaRepository<Server, String> {
    Optional<Server> findByUserIdAndServername(String userId, String servername);
}
