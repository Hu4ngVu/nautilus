package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.huangnosimp.nautilusserver.models.Owner;

public interface OwnerRepository extends JpaRepository<Owner, String> {
}
