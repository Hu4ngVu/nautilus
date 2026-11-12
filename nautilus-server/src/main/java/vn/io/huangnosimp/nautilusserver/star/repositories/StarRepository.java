package vn.io.huangnosimp.nautilusserver.star.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.star.models.Star;

@Repository
public interface StarRepository extends JpaRepository<Star, String> {
    Page<Star> findAllByServerId(String serverId, Pageable pageable);

    Page<Star> findAllByUserId(String userId, Pageable pageable);

    @Query("SELECT s.serverId FROM Star s WHERE s.userId = :userId")
    List<String> findServerIdsByUserId(@Param("userId") String userId);

    Page<Star> findAllByUserIdAndServerIdIn(
            String userId, Collection<String> serverIds, Pageable pageable);

    void deleteAllByServerId(String serverId);

    Optional<Star> findByUserIdAndServerId(String userId, String serverId);
}
