package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

@Repository
public interface GalleryRepository extends JpaRepository<Image, String> {
    @Query(
            "SELECT i.imageKey FROM Image i WHERE i.server.userId = :userId AND i.server.servername"
                    + " = :servername")
    List<String> findAllImageKeysByUserIdAndServername(
            @Param("userId") String userId, @Param("servername") String servername);

    Page<Image> findAllByServer(Server server, Pageable pageable);

    Optional<Image> findByServerAndId(Server server, String imageId);
}
