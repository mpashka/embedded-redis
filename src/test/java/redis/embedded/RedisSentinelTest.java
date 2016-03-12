package redis.embedded;

import com.google.common.collect.*;
import org.junit.*;
import redis.clients.jedis.*;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class RedisSentinelTest {
    private RedisSentinel sentinel;
    private RedisServer server;

    @Test(timeout = 3000L)
    @Configuration(sentinel = 1, master = 1)
    public void testSimpleRun() throws Exception {
        server = new RedisServer();
        sentinel = RedisSentinel.builder().build();

        sentinel.start();
        server.start();

        TimeUnit.SECONDS.sleep(1);

        server.stop();
        sentinel.stop();
    }

    @Test
    @Configuration(sentinel = 1)
    public void shouldAllowSubsequentRuns() throws Exception {
        sentinel = RedisSentinel.builder().build();

        sentinel.start();
        sentinel.stop();

        sentinel.start();
        sentinel.stop();

        sentinel.start();
        sentinel.stop();
    }

    @Test
    @Configuration(sentinel = 1, master = 1)
    public void testSimpleOperationsAfterRun() throws Exception {
        // Given
        server = new RedisServer();
        sentinel = RedisSentinel.builder().build();
        server.start();
        sentinel.start();
        TimeUnit.SECONDS.sleep(1);

        // When
        try (JedisSentinelPool pool = new JedisSentinelPool("myMaster", Sets.newHashSet("localhost:26379"));
             Jedis jedis = pool.getResource()) {

            jedis.mset("abc", "1", "def", "2");

            // Then
            assertThat(jedis.mget("abc"), contains("1"));
            assertThat(jedis.mget("def"), contains("2"));
            assertThat(jedis.mget("xyz"), contains(nullValue()));
        } finally {
            sentinel.stop();
            server.stop();
        }
    }
}