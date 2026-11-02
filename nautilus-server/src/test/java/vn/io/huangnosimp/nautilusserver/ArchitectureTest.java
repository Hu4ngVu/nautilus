package vn.io.huangnosimp.nautilusserver;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ArchitectureTest {
    ApplicationModules module = ApplicationModules.of(NautilusServerApplication.class);

    @Test
    void testArchitecture() {
        module.verify();
    }
}
