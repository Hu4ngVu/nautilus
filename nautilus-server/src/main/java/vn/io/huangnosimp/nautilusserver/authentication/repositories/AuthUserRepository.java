package vn.io.huangnosimp.nautilusserver.authentication.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.authentication.models.AuthUser;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, String> {
    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<AuthUser> findByEmail(String email);

    @Query("SELECT a.id FROM AuthUser a WHERE a.username = :username")
    Optional<String> findIdByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE AuthUser a SET a.username = :newUsername WHERE a.id = :userId")
    void updateUsernameById(
            @Param("userId") String userId, @Param("newUsername") String newUsername);

    @Query("SELECT a.id AS id, a.username AS username FROM AuthUser a WHERE a.id IN :ids")
    List<IdUsername> findIdAndUsernameByIdIn(@Param("ids") Collection<String> ids);

    interface IdUsername {
        String getId();

        String getUsername();
    }
}
