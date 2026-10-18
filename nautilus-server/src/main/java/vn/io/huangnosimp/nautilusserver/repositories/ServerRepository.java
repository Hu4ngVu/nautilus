package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.models.Server;

import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, String> {
    @Query("SELECT s.visibility FROM Server s WHERE s.user.username = :leaderName AND s.servername = :servername")
    Boolean findVisibilityByNameAndUsername(@Param("leaderName") String leaderName, @Param("servername") String servername);

    @Query("SELECT s FROM Server s WHERE s.user.username = :username AND s.servername = :servername")
    Optional<Server> findByUsernameAndServername(@Param("username") String username, @Param("servername") String servername);

    @Transactional
    @Modifying
    @Query("DELETE FROM Server s WHERE s.user.username = :username AND s.servername = :servername")
    void deleteByServernameAndUsername(@Param("username") String username, @Param("servername") String servername);

    @Query("SELECT EXISTS (SELECT 1 FROM Owner o WHERE o.user.username = :authenticationName AND o.server.user.username = :leaderName AND o.server.servername = :servername)")
    boolean existsOwnerUsername(@Param("authenticationName") String authenticationName, @Param("leaderName") String leaderName, @Param("servername") String servername);

    @Query("SELECT EXISTS (SELECT 1 FROM Server s WHERE s.user.username = :authenticationName AND s.servername = :servername)")
    boolean existsLeaderUsername(@Param("authenticationName") String authenticationName, @Param("servername") String servername);
}
