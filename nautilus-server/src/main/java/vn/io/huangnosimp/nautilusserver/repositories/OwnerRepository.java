package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.models.Owner;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, String> {
}
