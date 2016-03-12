package redis.embedded;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.embedded.util.JedisUtil;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SentinelClusterTest {

    private Redis sentinel1;
    private Redis sentinel2;

    private Redis master1;
    private Redis master2;

    private SentinelCluster instance;

    @Before
    public void setUp() throws Exception {
        sentinel1 = mock(Redis.class);
        sentinel2 = mock(Redis.class);

        master1 = mock(Redis.class);
        master2 = mock(Redis.class);
    }


    @Test
    @Configuration(sentinel = 2, master = 2)
    public void stopShouldStopEntireCluster() throws Exception {
        // Given
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new SentinelCluster(sentinels, servers);

        // When
        instance.stop();

        // Then
        for (Redis s : sentinels) {
            verify(s).stop();
        }
        for (Redis s : servers) {
            verify(s).stop();
        }
    }

    @Test
    @Configuration(sentinel = 2, master = 2)
    public void startShouldStartEntireCluster() throws Exception {
        // Given
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new SentinelCluster(sentinels, servers);

        // When
        instance.start();

        // Then
        for (Redis s : sentinels) {
            verify(s).start();
        }
        for (Redis s : servers) {
            verify(s).start();
        }
    }

    @Test
    @Configuration(sentinel = 2, master = 2)
    public void isActiveShouldCheckEntireClusterIfAllActive() throws Exception {
        // Given
        given(sentinel1.isActive()).willReturn(true);
        given(sentinel2.isActive()).willReturn(true);
        given(master1.isActive()).willReturn(true);
        given(master2.isActive()).willReturn(true);

        // And
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new SentinelCluster(sentinels, servers);

        // When
        instance.isActive();

        // Then
        for (Redis s : sentinels) {
            verify(s).isActive();
        }
        for (Redis s : servers) {
            verify(s).isActive();
        }
    }

    @Test
    @Configuration(master = 1, slave = 0)
    public void runWithSingleMasterNoSlavesCluster() throws Exception {
        //given
        HashSet<String> sentinelHosts = Sets.newHashSet("localhost:26379");
        final SentinelCluster cluster = SentinelCluster.builder().sentinelCount(1).replicationGroup("ourmaster", 0).build();
        cluster.start();

        //when
        testClusterWithOneMaster(sentinelHosts, cluster);
    }

    @Test
    @Configuration(master = 1, slave = 1)
    public void runWithSingleMasterAndOneSlave() throws Exception {
        //given
        HashSet<String> sentinelHosts = Sets.newHashSet("localhost:26379");
        final SentinelCluster cluster = SentinelCluster.builder().sentinelCount(1).replicationGroup("ourmaster", 1).build();
        cluster.start();

        //when
        testClusterWithOneMaster(sentinelHosts, cluster);
    }

    @Test
    @Configuration(master = 1, slave = 2)
    public void runWithSingleMasterMultipleSlaves() throws Exception {
        //given
        HashSet<String> sentinelHosts = Sets.newHashSet("localhost:26379");
        final SentinelCluster cluster = SentinelCluster.builder().sentinelCount(1).replicationGroup("ourmaster", 2).build();
        cluster.start();

        //when
        testClusterWithOneMaster(sentinelHosts, cluster);
    }

    @Test
    @Configuration(sentinel = 2, master = 1, slave = 2)
    public void runWithTwoSentinelsSingleMasterMultipleSlaves() throws Exception {
        //given
        HashSet<String> sentinelHosts = Sets.newHashSet("localhost:26379", "localhost:26380");
        final SentinelCluster cluster = SentinelCluster.builder().sentinelCount(2).replicationGroup("ourmaster", 2).build();
        cluster.start();

        //when
        testClusterWithOneMaster(sentinelHosts, cluster);
    }

    @Test
    @Configuration(sentinel = 2, master = 1, slave = 2)
    public void runWithTwoPredefinedSentinelsSingleMasterMultipleSlaves() throws Exception {
        //given
        List<Integer> sentinelPorts = Arrays.asList(26381, 26382);
        final SentinelCluster cluster = SentinelCluster.builder().sentinelPorts(sentinelPorts).replicationGroup("ourmaster", 2).build();
        cluster.start();
        final Set<String> sentinelHosts = JedisUtil.portsToJedisHosts(sentinelPorts);

        //when
        testClusterWithOneMaster(sentinelHosts, cluster);
    }

    @Test
    @Configuration(sentinel = 3, master = 3, slave = 3)
    public void runWithThreeSentinelsThreeMastersOneSlavePerMasterCluster() throws Exception {
        // Given
        final String[] masters = new String[]{"master1", "master2", "master3"};
        HashSet<String> sentinelHosts = Sets.newHashSet("localhost:26379", "localhost:26380", "localhost:26381");

        // And
        final SentinelCluster cluster = SentinelCluster.builder().sentinelCount(3).quorumSize(2)
                .replicationGroup(masters[0], 1)
                .replicationGroup(masters[1], 1)
                .replicationGroup(masters[2], 1)
                .build();
        cluster.start();

        // Then
        testClusterWithThreeMasters(masters, cluster, sentinelHosts);
    }

    @Test
    @Configuration(sentinel = 3, master = 3, slave = 3)
    public void runWithThreeSentinelsThreeMastersOneSlavePerMasterEphemeralCluster() throws Exception {
        //given
        final String[] masters = new String[]{"master1", "master2", "master3"};
        final SentinelCluster cluster = SentinelCluster.builder().ephemeral().sentinelCount(3).quorumSize(2)
                .replicationGroup(masters[0], 1)
                .replicationGroup(masters[1], 1)
                .replicationGroup(masters[2], 1)
                .build();
        cluster.start();
        final Set<String> sentinelHosts = JedisUtil.sentinelHosts(cluster);

        // When
        testClusterWithThreeMasters(masters, cluster, sentinelHosts);
    }

    // Helper
    private void testClusterWithThreeMasters(String[] masters, SentinelCluster cluster, Set<String> sentinelHosts) {
        try (JedisSentinelPool pool1 = new JedisSentinelPool(masters[0], sentinelHosts);
             JedisSentinelPool pool2 = new JedisSentinelPool(masters[1], sentinelHosts);
             JedisSentinelPool pool3 = new JedisSentinelPool(masters[2], sentinelHosts)) {

            thenTestPool(pool1);
            thenTestPool(pool2);
            thenTestPool(pool3);
        } finally {
            cluster.stop();
        }
    }

    // Helper
    private void testClusterWithOneMaster(Set<String> sentinelHosts, SentinelCluster cluster) {
        try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", sentinelHosts)) {
            thenTestPool(pool);
        } finally {
            cluster.stop();
        }
    }

    // Test Case
    private void thenTestPool(JedisSentinelPool pool) {
        try (Jedis jedis = pool.getResource()) {

            jedis.mset("abc", "1", "def", "2");

            //then
            assertEquals("1", jedis.mget("abc").get(0));
            assertEquals("2", jedis.mget("def").get(0));
            assertEquals(null, jedis.mget("xyz").get(0));
        }
    }

}