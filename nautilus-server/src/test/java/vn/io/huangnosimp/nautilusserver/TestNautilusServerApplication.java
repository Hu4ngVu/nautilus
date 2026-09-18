package vn.io.huangnosimp.nautilusserver;

import org.springframework.boot.SpringApplication;

public class TestNautilusServerApplication {

    public static void main(String[] args) {
        SpringApplication.from(NautilusServerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
