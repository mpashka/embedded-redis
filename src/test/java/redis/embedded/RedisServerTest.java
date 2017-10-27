package redis.embedded;

import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.embedded.exceptions.EmbeddedRedisException;
import redis.embedded.exceptions.RedisBuildingException;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;
import redis.embedded.util.OsArchitecture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

public class RedisServerTest {

    private RedisServer redisServer;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    Logger logger = LoggerFactory.getLogger(getClass());

    @After
    public void stopServer() {
        if (redisServer == null) {
            return;
        }
        if (redisServer.isActive()) {
            StringBuilder ports = new StringBuilder();
            for (Integer port : redisServer.ports()) {
                ports.append("port=").append(port).append(";");
            }
            logger.warn("Stopping redis server on ports: " + ports.toString());
            logger.warn("isActive = " + redisServer.isActive());

            StringBuilder args = new StringBuilder();
            for (String arg : redisServer.args) {
                args.append("port=").append(arg).append(";");
            }
            logger.warn("args = " + args.toString());

            redisServer.stop();
        } else if (redisServer.getState() == AbstractRedisInstance.State.failed) {
            logger.info("Server failed. Logs");
            InputStream errors = redisServer.errors();
            BufferedReader in = new BufferedReader(new InputStreamReader(errors));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    logger.info("    {}", s);
                }
            } catch (IOException e) {
                logger.warn("Internal error reading server log", e);
            }

        }
    }

    @Test
    public void testBuilderCopy() {
        RedisServer.Builder builder = new RedisServer.Builder();
        builder.setting("loglevel verbose");

        RedisServer.Builder copy = builder.copy();
        copy.setting("logfile /path/to/logs");

        assertThat(builder.settings(), not(equalTo(copy.settings())) );
    }

    @Test(timeout = 1500L)
    public void testSimpleRun() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
        assertEquals("State must be active", AbstractRedisInstance.State.active, redisServer.getState());

        TimeUnit.MILLISECONDS.sleep(500);

        redisServer.stop();
        assertEquals("State must be inactive", AbstractRedisInstance.State.inactive, redisServer.getState());
    }

    @Test(timeout = 1500L)
    public void testSimpleRunWithExecProvider() throws Exception {
        redisServer = new RedisServer(RedisExecProvider.build(), 6379);
        redisServer.start();

        TimeUnit.MILLISECONDS.sleep(500);

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
        RedisExecProvider customProvider = RedisExecProvider.build()
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
                .override(OS.WINDOWS, "some")
                .override(OS.MAC_OS_X, "some");

        exception.expect(RedisBuildingException.class);
        exception.expectMessage("Failed to resolve executable");

        redisServer = new RedisServer.Builder()
                .redisExecProvider(buggyProvider)
                .build();
    }

    @Test
    public void shouldLetAccessToLogs() throws IOException {
        redisServer = new RedisServer.Builder().build();
        redisServer.start();

        InputStream errors = redisServer.errors();
        BufferedReader in = new BufferedReader(new InputStreamReader(errors));

        List<String> list = new ArrayList<>(20);

        String s;
        do {
            s = in.readLine();
            list.add(s);
        } while (s != null);

        assertThat(list, hasItem(endsWith(" # Server started, Redis version 3.2.1")));
        assertThat(list, hasItem(endsWith(" * The server is now ready to accept connections on port 6379")));
    }

    @Test
    public void shouldLetAccessToLogsWhenError() throws IOException {
        redisServer = new RedisServer.Builder().build();
        redisServer.start();
        assertEquals("State must be active", AbstractRedisInstance.State.active, redisServer.getState());

        RedisServer server = new RedisServer.Builder().build();
        try {
            server.start();
        } catch (RuntimeException ignored) {
            assertEquals("State must be failed", AbstractRedisInstance.State.failed, server.getState());

        }

        InputStream errors = server.errors();
        BufferedReader in = new BufferedReader(new InputStreamReader(errors));

        List<String> list = new ArrayList<>();
        String s;
        do {
            s = in.readLine();
            list.add(s);
        } while (s != null);

        OsArchitecture os = OsArchitecture.detect();
        switch (os.os()) {
            case UNIX:
                assertThat(list, hasItem(endsWith(" # Creating Server TCP listening socket *:6379: bind: Address already in use")));
                break;
            case WINDOWS:
                assertThat(list, hasItem(endsWith(" # Creating Server TCP listening socket *:6379: bind: No such file or directory")));
                break;
            case MAC_OS_X:
                assertThat(list, hasItem(endsWith(" # Creating Server TCP listening socket *:6379: bind: Address already in use")));
                break;
        }
    }

    @Test
    public void shouldFailOnStartTimeout() throws Exception {
        String sleepExecutable = "sleep";
        if (OsArchitecture.detect().os() == OS.WINDOWS) {
            sleepExecutable = "timeout";
        }

        Constructor<RedisServer> constructor = RedisServer.class.getDeclaredConstructor(List.class, int.class);
        constructor.setAccessible(true);
        RedisServer server = constructor.newInstance(Arrays.asList(sleepExecutable, "10"), 0);
        server.setStartupTimeoutMs(500);

        long now = System.currentTimeMillis();
        try {
            server.start();
        } catch (RuntimeException e) {
            // That is expected
        }
        assertEquals("State must be failed", AbstractRedisInstance.State.failed, server.getState());
        assertEquals("Should wait AbstractRedisInstance.REDIS_STARTUP_TIMEOUT_MS and then fail", 600, 0.0 + System.currentTimeMillis() - now, 150);
    }



}
