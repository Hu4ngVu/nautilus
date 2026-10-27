package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

@Repository
public interface ServerRepository
        extends JpaRepository<Server, String>, JpaSpecificationExecutor<Server> {
    Optional<Server> findByUserIdAndServername(String userId, String servername);

    Page<Server> findAllByVisibilityIsTrue(Specification<Server> specification, Pageable pageable);
}
