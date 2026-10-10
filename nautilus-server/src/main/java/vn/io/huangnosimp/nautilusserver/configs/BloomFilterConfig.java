package vn.io.huangnosimp.nautilusserver.configs;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BloomFilterConfig {
    private final RedissonClient redissonClient;

    @Bean
    public RBloomFilter<String> usernameBloomFilter() {
        return redissonClient.getBloomFilter("bf:usernames", StringCodec.INSTANCE);
    }
}
