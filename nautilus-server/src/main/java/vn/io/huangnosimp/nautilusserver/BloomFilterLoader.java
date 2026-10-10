package vn.io.huangnosimp.nautilusserver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterLoader implements ApplicationRunner {
    private static final long EXPECTED_INSERTION = 1_000_000L;
    private static final double FALSE_POSITIVE = 0.01;
    private static final int BATCH_SIZE = 5000;

    private final RedissonClient redissonClient;
    private final RBloomFilter<String> filter;
    private final JdbcClient jdbcClient;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        RBucket<String> ready = redissonClient.getBucket("bf:username:ready", StringCodec.INSTANCE);
        if (ready.isExists()) {
            log.info("Bloom filter is already loaded, skipping");
            return;
        }
        RLock lock = redissonClient.getLock("lock:bf:username:load");
        lock.lock();
        try {
            if (ready.isExists()) {
                log.info("Bloom filter is already loaded, skipping");
            }
            log.info("Loading usernames into Bloom filter");
            filter.delete();
            filter.tryInit(EXPECTED_INSERTION, FALSE_POSITIVE);
            String last = "";
            int total = 0;
            while (true) {
                List<String> usernames = jdbcClient
                        .sql(
                                "SELECT username FROM user WHERE username > :last ORDER BY username LIMIT :batchSize"
                        )
                        .param("last", last)
                        .param("batchSize", BATCH_SIZE)
                        .query(String.class)
                        .list();
                if (usernames.isEmpty()) {
                    break;
                }
                filter.add(usernames.stream().map(String::toLowerCase).toList());
                last = usernames.getLast();
                total += usernames.size();
            }
            log.info("Loaded {} usernames into Bloom filter", total);
        } finally {
            lock.unlock();
        }
    }
}
