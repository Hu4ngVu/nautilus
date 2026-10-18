package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.models.Gallery;

import java.util.List;
import java.util.Optional;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, String> {
    @Query("SELECT g FROM Gallery g WHERE g.server.user.username = :username AND g.server.servername = :servername ORDER BY g.orderIndex ASC, g.createdAt ASC")
    List<Gallery> findAllByUsernameServername(@Param("username") String username, @Param("servername") String servername);

    @Modifying
    @Transactional
    @Query("DELETE FROM Gallery g WHERE g.server.user.username = :username AND g.server.servername = :servername AND g.id = :imageId")
    void deleteByUsernameServernameId(@Param("username") String username, @Param("servername") String servername, @Param("imageId") String imageId);

    @Query("SELECT g FROM Gallery g WHERE g.server.user.username = :username AND g.server.servername = :servername AND g.id = :imageId")
    Optional<Gallery> findByUsernameServernameId(String username, String servername, String imageId);
}
