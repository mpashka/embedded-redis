package redis.embedded;

import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.embedded.exceptions.EmbeddedRedisException;
import redis.embedded.exceptions.RedisBuildingException;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RedisServerTest {

    private RedisServer redisServer;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @After
    public void stopServer() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    @Test(timeout = 1500L)
    public void testSimpleRun() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();

        TimeUnit.SECONDS.sleep(1);

        redisServer.stop();
    }

    @Test
    public void shouldNotAllowMultipleRunsWithoutStop() throws Exception {
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();

            exception.expect(EmbeddedRedisException.class);
            exception.expectMessage("This redis server instance is already running...");
            redisServer.start();
        } finally {
            redisServer.stop();
        }
    }

    @Test
    public void shouldAllowSubsequentRuns() throws Exception {
        redisServer = new RedisServer(6379);

        redisServer.start();
        redisServer.stop();

        redisServer.start();
        redisServer.stop();

        redisServer.start();
        redisServer.stop();
    }

    @Test
    public void testSimpleOperationsAfterRun() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();

        try (JedisPool pool = new JedisPool("localhost", 6379);
             Jedis jedis = pool.getResource()) {

            jedis.mset("abc", "1", "def", "2");

            assertThat(jedis.mget("abc"), contains("1"));
            assertThat(jedis.mget("def"), contains("2"));
            assertThat(jedis.mget("xyz"), contains(nullValue()));
        }
    }

    @Test
    public void shouldIndicateInactiveBeforeStart() throws Exception {
        redisServer = new RedisServer(6379);

        assertThat(redisServer.isActive(), is(false));
    }

    @Test
    public void shouldIndicateActiveAfterStart() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();

        assertThat(redisServer.isActive(), is(true));
    }

    @Test
    public void shouldIndicateInactiveAfterStop() throws Exception {
        redisServer = new RedisServer(6379);

        redisServer.start();
        redisServer.stop();

        assertThat(redisServer.isActive(), is(false));
    }

    @Test
    public void shouldOverrideDefaultExecutable() throws Exception {
        RedisExecProvider customProvider = RedisExecProvider.defaultProvider()
                .override(OS.UNIX, Architecture.x86, Resources.getResource("fake-redis-server").getFile())
                .override(OS.UNIX, Architecture.x86_64, Resources.getResource("fake-redis-server").getFile())
                .override(OS.WINDOWS, Architecture.x86, Resources.getResource("fake-redis-server").getFile())
                .override(OS.WINDOWS, Architecture.x86_64, Resources.getResource("fake-redis-server").getFile())
                .override(OS.MAC_OS_X, Resources.getResource("fake-redis-server").getFile());

        redisServer = new RedisServer.Builder()
                .redisExecProvider(customProvider)
                .build();
    }

    @Test
    public void shouldFailWhenBadExecutableGiven() throws Exception {
        RedisExecProvider buggyProvider = RedisExecProvider.defaultProvider()
                .override(OS.UNIX, "some")
                .override(OS.WINDOWS, Architecture.x86, "some")
                .override(OS.WINDOWS, Architecture.x86_64, "some")
                .override(OS.MAC_OS_X, "some");

        exception.expect(RedisBuildingException.class);
        exception.expectMessage("Failed to resolve executable");

        redisServer = new RedisServer.Builder()
                .redisExecProvider(buggyProvider)
                .build();
    }
}
