package vn.io.huangnosimp.nautilusserver.user;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@TestConfiguration
@EnableJpaAuditing
@EntityScan(basePackageClasses = User.class)
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
public class UserTestConfig {}
