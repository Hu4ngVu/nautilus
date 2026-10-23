package vn.io.huangnosimp.nautilusserver.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.auth.models.AuthUser;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, String> {
    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<AuthUser> findByEmail(String email);

    @Query("SELECT a.id FROM AuthUser a WHERE a.username = :username")
    Optional<String> findIdByUsername(String username);
}
