package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

@Repository
public interface ServerRepository
        extends JpaRepository<Server, String>, JpaSpecificationExecutor<Server> {
    Optional<Server> findByUserIdAndServername(String userId, String servername);

    Page<Server> findAllByVisibilityIsTrue(Specification<Server> specification, Pageable pageable);

    Optional<Server> findByIdAndVisibilityIsTrue(String serverId);

    @Query(
            "SELECT s.id FROM Server s WHERE s.userId = :userId AND s.servername = :servername AND"
                    + " s.visibility = true")
    Optional<String> findServerId(
            @Param("userId") String userId, @Param("servername") String servername);

    @Modifying
    @Transactional
    @Query("UPDATE Server s SET s.starCount = s.starCount + :amount WHERE s.id = :serverId")
    void increaseStarCount(String serverId, Integer amount);

    @Modifying
    @Transactional
    @Query("UPDATE Server s SET s.starCount = s.starCount - :amount WHERE s.id = :serverId")
    void decreaseStarCount(String serverId, Integer amount);
}
