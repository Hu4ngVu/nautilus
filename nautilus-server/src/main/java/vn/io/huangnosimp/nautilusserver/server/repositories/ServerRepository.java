package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.Collection;
import java.util.List;
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
    boolean existsByIdAndVisibilityIsTrue(String serverId);

    List<Server> findAllByIdInAndVisibilityIsTrue(Collection<String> ids);

    @Query("SELECT s.id FROM Server s WHERE s.id IN :ids AND s.visibility = true")
    List<String> findPublicIdsByIdIn(@Param("ids") Collection<String> ids);

    Page<Server> findAllByVisibilityIsTrue(Specification<Server> specification, Pageable pageable);

    Optional<Server> findByIdAndVisibilityIsTrue(String serverId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Server s SET s.starCount = s.starCount + :amount WHERE s.id = :serverId")
    void increaseStarCount(@Param("serverId") String serverId, @Param("amount") Integer amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Server s SET s.starCount = s.starCount - :amount WHERE s.id = :serverId")
    void decreaseStarCount(@Param("serverId") String serverId, @Param("amount") Integer amount);

    boolean existsByUserIdAndServername(String userId, String servername);

    @Query("SELECT s.id FROM Server s WHERE s.userId = :userId AND s.servername = :servername")
    Optional<String> findServerId(
            @Param("userId") String userId, @Param("servername") String servername);

    Page<Server> findAllByUserId(String userId, Pageable pageable);

    Page<Server> findAllByUserIdAndVisibilityIsTrue(String userId, Pageable pageable);
}
