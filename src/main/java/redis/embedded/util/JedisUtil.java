package redis.embedded.util;

import redis.embedded.Redis;
import redis.embedded.SentinelCluster;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A bunch of helpers function to get Jedis hosts and ports
 * to use from different instances of embedded-redis.
 */
public abstract class JedisUtil {
    public static Set<String> jedisHosts(Redis redis) {
        final List<Integer> ports = redis.ports();
        return portsToJedisHosts(ports);
    }

    public static Set<String> sentinelHosts(SentinelCluster cluster) {
        final List<Integer> ports = cluster.sentinelPorts();
        return portsToJedisHosts(ports);
    }

    public static Set<String> portsToJedisHosts(List<Integer> ports) {
        Set<String> hosts = new HashSet<>();
        for (Integer p : ports) {
            hosts.add("localhost:" + p);
        }
        return hosts;
    }
}
