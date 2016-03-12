package redis.embedded;


import org.junit.*;
import org.junit.rules.*;
import redis.clients.jedis.*;
import redis.embedded.cluster.*;
import redis.embedded.exceptions.*;
import redis.embedded.util.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class RealRedisClusterTest {
    private Redis master1;
    private Redis master2;
    private Redis master3;
    private static final int DEFAULT_REPLICATES = 1;
    private static final int DEFAULT_NUMBER_RETRIES = 5;
    private static final Collection<Integer> ports = Arrays.asList(3000, 3001, 3002, 3003);
    private static final String LOCAL_HOST = "127.0.0.1";

    private RedisExecProvider redisExecProvider;
    private Redis instance;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        master1 = mock(Redis.class);
        master2 = mock(Redis.class);
        master3 = mock(Redis.class);

        redisExecProvider = RedisExecProvider.defaultProvider();
        redisExecProvider.override(OS.UNIX, "redis-server-3.0.7");
    }

    @After
    public void after() {
        if (instance != null) {
            instance.stop();
        }
    }

    @Test
    @Configuration(master = 1)
    public void numberOfNodeShouldAtLeastThree() throws Exception {
        final List<Redis> oneServer = Collections.singletonList(master1);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires at least 3 master nodes.");
        instance = new RedisCluster(oneServer, DEFAULT_REPLICATES, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 2)
    public void numberOfNodeShouldAtLeastThree2() throws Exception {
        final List<Redis> twoServers = Arrays.asList(master1, master2);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires at least 3 master nodes.");

        instance = new RedisCluster(twoServers, DEFAULT_REPLICATES, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 3)
    public void numberOfReplicatesShouldBeMoreThatOne() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires at least 1 replication.");

        instance = new RedisCluster(threeServers, 0, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 3, slave = 10)
    public void numberOfReplicatesShouldBeLessThanNumberOfServers() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires number of replications less than (number of nodes - 1).");

        instance = new RedisCluster(threeServers, 10, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void numberOfRetriesShouldBeMoreThanZero() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires number of retries more than zero.");

        instance = new RedisCluster(threeServers, DEFAULT_REPLICATES, 0);
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void isActiveShouldCheckEntireClusterIfAllActive() throws Exception {
        instance = new RedisCluster.Builder()
                .withServerBuilder(new RedisServer.Builder()
                        .redisExecProvider(redisExecProvider))
                .serverPorts(ports).build();

        instance.start();

        assertThat(instance.isActive(), equalTo(true));
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void startShouldStartCluster() throws Exception {
        instance = new RedisCluster.Builder()
                .withServerBuilder(new RedisServer.Builder()
                        .redisExecProvider(redisExecProvider))
                .serverPorts(ports).build();

        Set<HostAndPort> hostAndPorts = new HashSet<>(ports.size());

        for (Integer port : ports) {
            hostAndPorts.add(new HostAndPort(LOCAL_HOST, port));
        }

        instance.start();
        try (JedisCluster jc = new JedisCluster(hostAndPorts)) {
            jc.hset("key", "field", "value");

            assertThat(jc.hget("key", "field"), equalTo("value"));
        }
    }
}
