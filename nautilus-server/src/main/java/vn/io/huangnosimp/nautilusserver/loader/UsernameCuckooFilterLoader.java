package vn.io.huangnosimp.nautilusserver.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import vn.io.huangnosimp.nautilusserver.constants.CuckooFilter;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameCuckooFilterLoader implements ApplicationRunner {
    private static final long EXPECTED_INSERTION = 1_000_000L;
    private static final int BATCH_SIZE = 5000;

    private final RedissonClient redissonClient;
    @Qualifier("usernameCuckooFilter")
    private final RCuckooFilter<String> filter;
    private final JdbcClient jdbcClient;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        RBucket<String> ready = redissonClient.getBucket(CuckooFilter.username.BUCKET, StringCodec.INSTANCE);
        if (ready.isExists()) {
            log.info("Bloom filter is already loaded, skipping");
            return;
        }
        RLock lock = redissonClient.getLock(CuckooFilter.username.LOCK);
        lock.lock();
        try {
            if (ready.isExists()) {
                log.info("Bloom filter is already loaded, skipping");
                return;
            }
            log.info("Loading usernames into Bloom filter");
            filter.delete();
            filter.init(EXPECTED_INSERTION);
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

                RBatch batch = redissonClient.createBatch();
                RCuckooFilterAsync<String> asyncFilter = batch.getCuckooFilter(CuckooFilter.username.NAME, StringCodec.INSTANCE);

                last = usernames.getLast();
                for (String username : usernames) {
                    asyncFilter.addAsync(username);
                }
                batch.execute();
                total += usernames.size();
            }
            ready.set("1");
            log.info("Loaded {} usernames into Bloom filter", total);
        } finally {
            lock.unlock();
        }
    }
}
