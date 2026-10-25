package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.server.models.Image;

@Repository
public interface GalleryRepository extends JpaRepository<Image, String> {
    @Query(
            "SELECT g FROM Image g WHERE g.server.userId = :userId AND g.server.servername ="
                    + " :servername ORDER BY g.orderIndex ASC, g.createdAt ASC")
    List<Image> findAllByUserIdServername(
            @Param("authUserId") String userId, @Param("servername") String servername);

    @Query(
            "SELECT g FROM Image g WHERE g.server.userId = :userId AND g.server.servername ="
                    + " :servername AND g.id = :imageId")
    Optional<Image> findByUserIdServernameId(String userId, String servername, String imageId);

    @Query(
            "SELECT i.imageKey FROM Image i WHERE i.server.userId = :userId AND i.server.servername"
                    + " = :servername")
    List<String> findAllImageKeysByUserIdAndServername(String userId, String servername);
}
