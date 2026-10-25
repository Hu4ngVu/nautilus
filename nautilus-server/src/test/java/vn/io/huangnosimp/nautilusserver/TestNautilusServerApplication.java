package vn.io.huangnosimp.nautilusserver;

import org.springframework.boot.SpringApplication;

public class TestNautilusServerApplication {

    static void main(String[] args) {
        SpringApplication.from(NautilusServerApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
