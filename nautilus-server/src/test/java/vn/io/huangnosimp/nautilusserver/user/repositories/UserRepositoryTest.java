package vn.io.huangnosimp.nautilusserver.user.repositories;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import vn.io.huangnosimp.nautilusserver.user.UserTestConfig;
import vn.io.huangnosimp.nautilusserver.user.models.User;

@DataJpaTest
@ActiveProfiles("test")
@Import(UserTestConfig.class)
public class UserRepositoryTest {
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void saveUser() {
        User testUser =
                User.builder()
                        .authUserId("testAuthUserId")
                        .username("testUsername")
                        .serverCount(0)
                        .build();
        userRepository.save(testUser);
    }

    @Test
    void findByAuthUserId_ShouldReturnUser() {
        User user = userRepository.findByAuthUserId("testAuthUserId").orElse(null);
        assertNotNull(user);
        assertEquals("testAuthUserId", user.getAuthUserId());
        assertEquals("testUsername", user.getUsername());
        assertEquals(0, user.getServerCount());
    }

    @Test
    void increaseServerCount_ShouldIncreaseCount() {
        userRepository.increaseServerCount("testAuthUserId", 5);
        User user = userRepository.findByAuthUserId("testAuthUserId").orElse(null);
        assertNotNull(user);
        assertEquals(5, user.getServerCount());
    }

    @Test
    void increaseServerCount_ShouldIncreaseCountMultipleTimes() {
        userRepository.increaseServerCount("testAuthUserId", 3);
        userRepository.increaseServerCount("testAuthUserId", 2);
        User user = userRepository.findByAuthUserId("testAuthUserId").orElse(null);
        assertNotNull(user);
        assertEquals(5, user.getServerCount());
    }

    @Test
    void decreaseServerCount_ShouldDecreaseCount() {
        userRepository.increaseServerCount("testAuthUserId", 5);
        userRepository.decreaseServerCount("testAuthUserId", 2);
        User user = userRepository.findByAuthUserId("testAuthUserId").orElse(null);
        assertNotNull(user);
        assertEquals(3, user.getServerCount());
    }

    @Test
    void decreaseServerCount_ShouldNotGoBelowZero() {
        userRepository.decreaseServerCount("testAuthUserId", 1);
        User user = userRepository.findByAuthUserId("testAuthUserId").orElse(null);
        assertNotNull(user);
        assertEquals(0, user.getServerCount());
    }

    @Test
    void decreaseServerCount_ShouldDecreaseCountMultipleTimes() {
        userRepository.increaseServerCount("testAuthUserId", 5);
        userRepository.decreaseServerCount("testAuthUserId", 2);
        userRepository.decreaseServerCount("testAuthUserId", 1);
        User user = userRepository.findByAuthUserId("testAuthUserId").orElse(null);
        assertNotNull(user);
        assertEquals(2, user.getServerCount());
    }

    @Test
    void existsByUsername_ShouldReturnTrueForExistingUsername() {
        boolean exists = userRepository.existsByUsername("testUsername");
        assertTrue(exists);
    }

    @Test
    void existsByUsername_ShouldReturnFalseForNonExistingUsername() {
        boolean exists = userRepository.existsByUsername("nonExistingUsername");
        assertFalse(exists);
    }
}
