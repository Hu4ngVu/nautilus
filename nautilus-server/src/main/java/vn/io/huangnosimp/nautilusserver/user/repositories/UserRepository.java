package vn.io.huangnosimp.nautilusserver.user.repositories;

import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.user.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByAuthUserId(String authUserId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.serverCount = u.serverCount + :amount WHERE u.authUserId = :userId")
    void increaseServerCount(@Param("userId") String userId, @Param("amount") Integer amount);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.serverCount = u.serverCount - :amount WHERE u.authUserId = :userId")
    void decreaseServerCount(@Param("userId") String userId, @Param("amount") Integer amount);

    boolean existsByUsername(@NotBlank String s);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.username = :newUsername WHERE u.username = :oldUsername")
    void updateUsername(
            @Param("oldUsername") String oldUsername,
            @Param("newUsername") @NotBlank String newUsername);
}
