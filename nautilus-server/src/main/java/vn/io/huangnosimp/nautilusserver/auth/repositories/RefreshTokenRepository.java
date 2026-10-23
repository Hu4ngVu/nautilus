package vn.io.huangnosimp.nautilusserver.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.auth.models.AuthUser;
import vn.io.huangnosimp.nautilusserver.auth.models.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByAuthUser(AuthUser user);
}
