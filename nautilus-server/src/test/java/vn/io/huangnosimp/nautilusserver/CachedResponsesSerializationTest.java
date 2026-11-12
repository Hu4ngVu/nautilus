package vn.io.huangnosimp.nautilusserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerPlatform;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.ServerInfoDetailResponse;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;

/**
 * Values returned from {@code @Cacheable} methods must survive the Redis cache's value serializer.
 */
class CachedResponsesSerializationTest {
    private final SerializationPair<Object> values =
            RedisCacheConfiguration.defaultCacheConfig().getValueSerializationPair();

    @Test
    void userInfoResponseRoundTripsThroughCacheSerializer() {
        var response = new UserInfoResponse("alice", 2, "bio", "https://cdn/avatar.gif");
        assertEquals(response, values.read(values.write(response)));
    }

    @Test
    void serverInfoDetailResponseRoundTripsThroughCacheSerializer() {
        var response =
                ServerInfoDetailResponse.builder()
                        .servername("demo")
                        .visibility(true)
                        .requiresPremium(false)
                        .tags(List.of("PvP"))
                        .serverType(ServerType.VANILLA)
                        .serverPlatform(ServerPlatform.JAVA)
                        .starCount(3)
                        .build();
        assertEquals(response, values.read(values.write(response)));
    }
}
