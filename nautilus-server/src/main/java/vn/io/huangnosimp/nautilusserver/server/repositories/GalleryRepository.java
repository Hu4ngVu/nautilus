package vn.io.huangnosimp.nautilusserver.server.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.server.models.Image;
import vn.io.huangnosimp.nautilusserver.server.models.Server;

@Repository
public interface GalleryRepository extends JpaRepository<Image, String> {
    List<Image> findAllByServer(Server server);

    Page<Image> findAllByServer(Server server, Pageable pageable);

    Optional<Image> findByServerAndId(Server server, String imageId);
}
