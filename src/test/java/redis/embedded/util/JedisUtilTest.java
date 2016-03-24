package redis.embedded.util;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import redis.embedded.RedisServer;
import redis.embedded.SentinelCluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JedisUtilTest {

    private static Set<String> hosts = new HashSet<>();

    static {
        hosts.add("localhost:1");
        hosts.add("localhost:2");
        hosts.add("localhost:3");
    }

    @Test
    public void jedisHosts() {
        RedisServer server = new RedisServer.Builder().port(1).build();
        Set<String> host = new HashSet<>();
        host.add("localhost:1");

        assertThat(JedisUtil.jedisHosts(server), equalTo(host));
    }

    @Test
    public void sentinelHosts() {
        SentinelCluster sentinelCluster = new SentinelCluster.Builder()
                .sentinelPorts(Arrays.asList(1, 2, 3))
                .build();

        assertThat(JedisUtil.sentinelHosts(sentinelCluster), equalTo(hosts));
    }

    @Test
    public void portsToJedisHosts() {
        List<Integer> integers = Arrays.asList(1, 2, 3);

        assertThat(JedisUtil.portsToJedisHosts(integers), equalTo(hosts));
    }
}
