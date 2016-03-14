package redis.embedded.cluster;

import org.slf4j.*;
import redis.embedded.*;
import redis.embedded.exceptions.*;

import java.io.*;
import java.util.*;

public class RedisCluster implements Redis {
    private final Logger logger = LoggerFactory.getLogger(RedisCluster.class);

    private static final int CLUSTER_HASH_SLOTS_NUMBER = 16384;
    private static final String LOCAL_HOST = "127.0.0.1";

    private final List<Redis> servers = new LinkedList<>();
    private final int numOfReplicates;
    private final int maxNumOfRetries;

    public RedisCluster(List<Redis> servers, int numOfReplicates, int maxNumOfRetries) {
        this.numOfReplicates = numOfReplicates;
        this.maxNumOfRetries = maxNumOfRetries;
        this.servers.addAll(servers);
        validateParams();
    }

    private void validateParams() {
        if (servers.size() <= 2) {
            throw new EmbeddedRedisException("Redis Cluster requires at least 3 master nodes.");
        }
        if (numOfReplicates < 1) {
            throw new EmbeddedRedisException("Redis Cluster requires at least 1 replication.");
        }
        if (numOfReplicates > servers.size() - 1) {
            throw new EmbeddedRedisException("Redis Cluster requires number of replications less than (number of nodes - 1).");
        }
        if (maxNumOfRetries < 1) {
            throw new EmbeddedRedisException("Redis Cluster requires number of retries more than zero.");
        }
    }

    @Override
    public void start() throws EmbeddedRedisException {
        for (Redis redis : servers) {
            redis.start();
        }

        List<MasterNode> masters = allocSlots();
        joinCluster();

        logger.debug("Starting redis cluster (waiting for the cluster to join)");
        int numRetried = 0;
        while (!isClusterActive()) {
            try {
                Thread.sleep(1000);
                numRetried++;
                if (numRetried == maxNumOfRetries) {
                    throw new EmbeddedRedisException("Redis cluster have not started after " + (numRetried + 1) + " seconds.");
                }
            } catch (InterruptedException e) {
                throw new EmbeddedRedisException(e.getMessage(), e);
            }
        }
        logger.debug("Redis cluster started");

        setReplicates(masters);
    }

    @Override
    public boolean isActive() {
        for (Redis redis : servers) {
            if (!redis.isActive()) {
                return false;
            }
        }

        return isClusterActive();
    }

    @Override
    public void stop() throws EmbeddedRedisException {
        for (Redis redis : servers) {
            redis.stop();
        }
    }

    @Override
    public List<Integer> ports() {
        List<Integer> ports = new ArrayList<>();
        for (Redis redis : servers) {
            ports.addAll(redis.ports());
        }
        return ports;
    }

    @Override
    public void errors(OutputStream outputStream) {
        for (Redis server : servers) {
            server.errors(outputStream);
        }
    }

    private boolean isClusterActive() {
        return clusterState().equals(ClusterState.OK);
    }

    private ClusterState clusterState() {
        Redis redis = servers.get(0);
        try (Client client = new Client(LOCAL_HOST, redis.ports().get(0))) {
            String ack = client.clusterInfo();
            return ClusterState.getStateByStr(ack.split("\r\n")[0].split(":")[1]);
        }
    }

    private void joinCluster() {
        int portFirstServer = servers.get(0).ports().get(0);

        //connect sequentially node i to node 0
        for (int i = 1; i < servers.size(); i++) {
            try (Client client = new Client(LOCAL_HOST, servers.get(i).ports().get(0))) {
                client.clusterMeet(LOCAL_HOST, portFirstServer);
            }
        }
    }

    private List<MasterNode> allocSlots() {
        int nodesCount = servers.size();
        int mastersCount = nodesCount / (numOfReplicates + 1);

        List<MasterNode> masters = new ArrayList<>(mastersCount);

        // alloc slots on masters
        int slotPerNode = CLUSTER_HASH_SLOTS_NUMBER / mastersCount;
        int first = 0;
        double cursor = 0.0;
        for (int i = 0; i < mastersCount; i++) {
            int last = (int) Math.round(cursor + slotPerNode - 1);
            if (last > CLUSTER_HASH_SLOTS_NUMBER || i == mastersCount - 1) {
                last = CLUSTER_HASH_SLOTS_NUMBER - 1;
            }

            //Min step is 1.
            if (last < first)
                last = first;

            masters.add(new MasterNode(servers.get(i), new SlotRange(first, last)));
            first = last + 1;
            cursor += slotPerNode;
        }

        int iter = 0;
        // Select N replicas for every master.
        for (int i = mastersCount; i < servers.size(); i++) {
            masters.get(iter).addSlave(servers.get(i));
            if (iter == mastersCount - 1) {
                iter = 0;
            } else {
                iter++;
            }
        }


        for (MasterNode master : masters) {
            try (Client jedis = new Client(LOCAL_HOST, master.getMaster().ports().get(0))) {
                // Add slots
                jedis.clusterAddSlots(master.getSlotRange().getRange());

                // Get node id
                String curNodeId = jedis.getNodeId();

                logger.debug("Master node: {} with slots {}", curNodeId, master.getSlotRange());

                master.setNodeId(curNodeId);
            }
        }
        return masters;
    }

    private void setReplicates(List<MasterNode> masters) {
        for (MasterNode master : masters) {
            setSlaves(master.getNodeId(), master.getSlaves());
        }
    }

    private void setSlaves(String masterNodeId, Set<Redis> slaves) {
        for (Redis slave : slaves) {
            try (Client client = new Client(LOCAL_HOST, slave.ports().get(0))) {
                client.clusterReplicate(masterNodeId);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private static final int DEFAULT_REPLICATES = 1;
        private static final int DEFAULT_NUMBER_RETRIES = 5;
        Collection<Integer> ports;
        private int numOfReplicates;
        private int numOfRetries;
        private int connectionTimeout;

        private RedisServer.Builder serverBuilder = new RedisServer.Builder();


        public Builder withServerBuilder(RedisServer.Builder serverBuilder) {
            this.serverBuilder = serverBuilder;
            return this;
        }

        public Builder serverPorts(Collection<Integer> ports) {
            this.ports = ports;
            return this;
        }

        public Builder numOfReplicates(int numOfReplicates) {
            this.numOfReplicates = numOfReplicates;
            return this;
        }

        public Builder numOfRetries(int numOfRetries) {
            this.numOfRetries = numOfRetries;
            return this;
        }

        public RedisCluster build() {
            final List<Redis> servers = buildServers();
            if (numOfReplicates == 0) {
                numOfReplicates = DEFAULT_REPLICATES;
            }
            if (numOfRetries == 0) {
                numOfRetries = DEFAULT_NUMBER_RETRIES;
            }
            return new RedisCluster(servers, numOfReplicates, numOfRetries);
        }

        private List<Redis> buildServers() {
            List<Redis> servers = new ArrayList<>();
            for (Integer port : ports) {
                servers.add(buildNode(port));
            }
            return servers;
        }

        private RedisServer buildNode(Integer port) {
            serverBuilder.reset();
            serverBuilder.setting("cluster-enabled yes");
            serverBuilder.setting("cluster-config-file nodes-" + port + ".conf");
            serverBuilder.setting("cluster-node-timeout 2000");
            serverBuilder.setting("appendonly yes");
            serverBuilder.setting("dbfilename dump-" + port + ".rdb");
            return serverBuilder.port(port).build();
        }
    }

}
