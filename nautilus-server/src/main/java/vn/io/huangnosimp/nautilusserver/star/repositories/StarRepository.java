package vn.io.huangnosimp.nautilusserver.star.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.star.models.Star;

@Repository
public interface StarRepository extends JpaRepository<Star, String> {
    Page<Star> findAllByServerId(String serverId, Pageable pageable);

    Page<Star> findAllByUserId(String userId, Pageable pageable);

    void deleteAllByServerId(String serverId);

    Optional<Star> findByUserIdAndServerId(String userId, String serverId);
}
