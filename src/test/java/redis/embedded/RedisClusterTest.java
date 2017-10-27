package redis.embedded;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.embedded.cluster.RedisCluster;
import redis.embedded.exceptions.EmbeddedRedisException;
import redis.embedded.ports.SequencePortProvider;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class RedisClusterTest {
    private Redis master1;
    private Redis master2;
    private Redis master3;
    private static final int DEFAULT_REPLICATES = 1;
    private static final int DEFAULT_NUMBER_RETRIES = 5;
    private static final Collection<Integer> ports = Arrays.asList(3000, 3001, 3002, 3003);
    private static final String LOCAL_HOST = "127.0.0.1";

    private Redis instance;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        master1 = mock(Redis.class);
        master2 = mock(Redis.class);
        master3 = mock(Redis.class);
    }

    @After
    public void after() {
        if (instance != null) {
            instance.stop();
        }
    }

    @Test
    @Configuration(master = 1)
    public void numberOfNodeShouldAtLeastThree() {
        final List<Redis> oneServer = Collections.singletonList(master1);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires at least 3 master nodes.");
        instance = new RedisCluster(oneServer, DEFAULT_REPLICATES, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 2)
    public void numberOfNodeShouldAtLeastThree2() {
        final List<Redis> twoServers = Arrays.asList(master1, master2);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires at least 3 master nodes.");

        instance = new RedisCluster(twoServers, DEFAULT_REPLICATES, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 3)
    public void numberOfReplicatesShouldBeMoreThatOne() {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires at least 1 replication.");

        instance = new RedisCluster(threeServers, 0, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 3, slave = 10)
    public void numberOfReplicatesShouldBeLessThanNumberOfServers() {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires number of replications less than (number of nodes - 1).");

        instance = new RedisCluster(threeServers, 10, DEFAULT_NUMBER_RETRIES);
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void numberOfRetriesShouldBeMoreThanZero() {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("Redis Cluster requires number of retries more than zero.");

        instance = new RedisCluster(threeServers, DEFAULT_REPLICATES, 0);
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void isActiveShouldCheckEntireClusterIfAllActive() {
        instance = new RedisCluster.Builder()
                .serverPorts(ports).build();

        instance.start();

        assertThat(instance.isActive(), equalTo(true));
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void startShouldStartCluster() throws IOException {
        instance = new RedisCluster.Builder()
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

    @Test
    @Configuration(master = 3, slave = 1)
    public void startShouldStartClusterWithCustomSettings() throws IOException {
        instance = new RedisCluster.Builder()
                .serverPorts(ports).setting("maxmemory 128M").build();

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

    @Test
    @Configuration(master = 3, slave = 1)
    public void portsShouldReplacePortProvider() {
        final PortProvider portProvider = new SequencePortProvider(3100, 3103);

        instance = new RedisCluster.Builder()
                .serverPorts(ports)
                .serverPorts(portProvider)
                .numOfMasters(3)
                .build();

        assertThat(instance.ports(), equalTo(ports));
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void portProvidersAreUsedToCreateCluster() {
        final PortProvider portProvider = new SequencePortProvider(3000, 3003);

        instance = new RedisCluster.Builder()
                .serverPorts(portProvider)
                .numOfMasters(3)
                .build();

        assertThat(instance.ports(), equalTo(ports));
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void portProviderNeedsTheNumberOfMasterToBeSpecified() {
        final PortProvider portProvider = new SequencePortProvider(3000, 3003);

        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("RedisCluster.Builder requires the number of master with a port provider.");

        instance = new RedisCluster.Builder()
                .serverPorts(portProvider)
                .build();
    }

    @Test
    @Configuration(master = 3, slave = 1)
    public void shouldThrowIfNonePortsAreProvided() {
        exception.expect(EmbeddedRedisException.class);
        exception.expectMessage("RedisCluster.Builder requires a port provider or a ports collection.");

        instance = new RedisCluster.Builder()
                .build();
    }
}
