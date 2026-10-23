package vn.io.huangnosimp.nautilusserver.auth.loader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import vn.io.huangnosimp.nautilusserver.auth.constants.CuckooFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailCuckooFilterLoader implements ApplicationRunner {
    private static final long EXPECTED_INSERTION = 1_000_000L;
    private static final int BATCH_SIZE = 5000;

    private final RedissonClient redissonClient;
    @Qualifier("emailCuckooFilter")
    private final RCuckooFilter<String> filter;
    private final JdbcClient jdbcClient;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        RBucket<String> ready = redissonClient.getBucket(CuckooFilter.email.BUCKET, StringCodec.INSTANCE);
        if (ready.isExists()) {
            log.info("Email Cuckoo filter is already loaded, skipping");
            return;
        }
        RLock lock = redissonClient.getLock(CuckooFilter.email.LOCK);
        lock.lock();
        try {
            if (ready.isExists()) {
                log.info("Email Cuckoo filter is already loaded, skipping");
                return;
            }
            log.info("Loading emails into Cuckoo filter");
            filter.delete();
            filter.init(EXPECTED_INSERTION);
            String last = "";
            int total = 0;
            while (true) {
                var emails = jdbcClient
                        .sql(
                                "SELECT email FROM user WHERE email > :last ORDER BY email LIMIT :batchSize"
                        )
                        .param("last", last)
                        .param("batchSize", BATCH_SIZE)
                        .query(String.class)
                        .list();
                if (emails.isEmpty()) {
                    break;
                }

                RBatch batch = redissonClient.createBatch();
                RCuckooFilterAsync<String> asyncFilter = batch.getCuckooFilter(CuckooFilter.email.NAME, StringCodec.INSTANCE);

                last = emails.getLast();
                for (String email : emails) {
                    asyncFilter.addAsync(email);
                }
                batch.execute();
                total += emails.size();
            }
            log.info("Loaded {} emails into Cuckoo filter", total);
            ready.set("1");
        } finally {
            lock.unlock();
        }
    }
}
