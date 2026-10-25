package vn.io.huangnosimp.nautilusserver;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableCaching
@EnableJpaAuditing
@EnableAsync
public class AppConfig {}
