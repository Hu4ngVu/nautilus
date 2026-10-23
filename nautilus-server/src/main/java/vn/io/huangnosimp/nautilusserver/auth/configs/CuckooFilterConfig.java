package vn.io.huangnosimp.nautilusserver.auth.configs;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RCuckooFilter;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.io.huangnosimp.nautilusserver.auth.constants.CuckooFilter;

@Configuration
@RequiredArgsConstructor
public class CuckooFilterConfig {
    private final RedissonClient redissonClient;

    @Bean(name = "usernameCuckooFilter")
    public RCuckooFilter<String> usernameCuckooFilter() {
        return redissonClient.getCuckooFilter(CuckooFilter.username.NAME, StringCodec.INSTANCE);
    }

    @Bean(name = "emailCuckooFilter")
    public RCuckooFilter<String> emailCuckooFilter() {
        return redissonClient.getCuckooFilter(CuckooFilter.email.NAME, StringCodec.INSTANCE);
    }
}
