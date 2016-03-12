package redis.embedded.cluster;

import redis.clients.jedis.*;

import java.io.*;

// TODO Remove dependency on Jedis
class Client implements Closeable {

    private final Jedis jedis;

    Client(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    public String clusterReplicate(final String nodeId) {
        return jedis.clusterReplicate(nodeId);
    }

    public void close() {
        jedis.close();
    }

    public String clusterAddSlots(final int... slots) {
        return jedis.clusterAddSlots(slots);
    }

    public String clusterNodes() {
        return jedis.clusterNodes();
    }

    public String getNodeId() {
        return clusterNodes().split(" :")[0];
    }

    public String clusterMeet(final String ip, final int port) {
        return jedis.clusterMeet(ip, port);
    }

    public String clusterInfo() {
        return jedis.clusterInfo();
    }
}
