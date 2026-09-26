package vn.io.huangnosimp.nautilusserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NautilusServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NautilusServerApplication.class, args);
    }

}
