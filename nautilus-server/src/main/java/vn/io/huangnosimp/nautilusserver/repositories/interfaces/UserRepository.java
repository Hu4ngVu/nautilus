package vn.io.huangnosimp.nautilusserver.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.huangnosimp.nautilusserver.models.User;

public interface UserRepository extends JpaRepository<User, String> {

}
