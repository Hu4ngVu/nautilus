package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.models.Server;

import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, String> {
    @Query("SELECT EXISTS (SELECT 1 FROM Server s WHERE s.name = :name AND s.user.username = :username)")
    boolean existsByNameAndUserUsername(@Param("name") String name, @Param("newUsername") String username);

    @Query("SELECT s.visibility FROM Server s WHERE s.name = :name AND s.user.username = :username")
    Boolean findVisibilityByNameAndUserUsername(@Param("name") String name, @Param("newUsername") String username);

    Optional<Server> findByNameAndUserUsername(String name, String username);

    void deleteByNameAndUserUsername(String name, String username);
}
