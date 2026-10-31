package vn.io.huangnosimp.nautilusserver.auth.loader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import vn.io.huangnosimp.nautilusserver.auth.constants.CuckooFilter;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

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
    @Logging(level = Level.DEBUG)
    public void run(@NonNull ApplicationArguments args) {
        RBucket<String> ready =
                redissonClient.getBucket(CuckooFilter.email.BUCKET, StringCodec.INSTANCE);
        if (ready.isExists()) {
            return;
        }
        RLock lock = redissonClient.getLock(CuckooFilter.email.LOCK);
        lock.lock();
        try {
            if (ready.isExists()) {
                return;
            }
            filter.delete();
            filter.init(EXPECTED_INSERTION);
            String last = "";
            while (true) {
                var emails =
                        jdbcClient
                                .sql(
                                        "SELECT email FROM auth_user WHERE email > :last ORDER BY email"
                                                + " LIMIT :batchSize")
                                .param("last", last)
                                .param("batchSize", BATCH_SIZE)
                                .query(String.class)
                                .list();
                if (emails.isEmpty()) {
                    break;
                }

                RBatch batch = redissonClient.createBatch();
                RCuckooFilterAsync<String> asyncFilter =
                        batch.getCuckooFilter(CuckooFilter.email.NAME, StringCodec.INSTANCE);

                last = emails.getLast();
                for (String email : emails) {
                    asyncFilter.addAsync(email);
                }
                batch.execute();
            }
            ready.set("1");
        } finally {
            lock.unlock();
        }
    }
}
