package redis.embedded;

import com.google.common.collect.Lists;
import redis.embedded.exceptions.EmbeddedRedisException;
import redis.embedded.ports.EphemeralPortProvider;
import redis.embedded.ports.PredefinedPortProvider;
import redis.embedded.ports.SequencePortProvider;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;

@SuppressWarnings("unused")
public class SentinelCluster implements Redis {
    private final List<Redis> sentinels = new LinkedList<>();
    private final List<Redis> servers = new LinkedList<>();

    SentinelCluster(List<Redis> sentinels, List<Redis> servers) {
        this.servers.addAll(servers);
        this.sentinels.addAll(sentinels);
    }

    @Override
    public boolean isActive() {
        for (Redis redis : sentinels) {
            if (!redis.isActive()) {
                return false;
            }
        }
        for (Redis redis : servers) {
            if (!redis.isActive()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void start() throws EmbeddedRedisException {
        for (Redis redis : sentinels) {
            redis.start();
        }
        for (Redis redis : servers) {
            redis.start();
        }
    }

    @Override
    public void stop() throws EmbeddedRedisException {
        for (Redis redis : sentinels) {
            redis.stop();
        }
        for (Redis redis : servers) {
            redis.stop();
        }
    }

    @Override
    public List<Integer> ports() {
        List<Integer> ports = new ArrayList<>();
        ports.addAll(sentinelPorts());
        ports.addAll(serverPorts());
        return ports;
    }

    @Override
    public InputStream errors() {
        List<InputStream> inputStreams = new ArrayList<>();
        for (Redis redis : sentinels) {
            inputStreams.add(redis.errors());
        }
        for (Redis redis : servers) {
            inputStreams.add(redis.errors());
        }

        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }

    public List<Redis> sentinels() {
        return Lists.newLinkedList(sentinels);
    }

    public List<Integer> sentinelPorts() {
        List<Integer> ports = new ArrayList<>();
        for (Redis redis : sentinels) {
            ports.addAll(redis.ports());
        }
        return ports;
    }

    public List<Redis> servers() {
        return Lists.newLinkedList(servers);
    }

    public List<Integer> serverPorts() {
        List<Integer> ports = new ArrayList<>();
        for (Redis redis : servers) {
            ports.addAll(redis.ports());
        }
        return ports;
    }

    @Deprecated
    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private RedisSentinel.Builder sentinelBuilder = new RedisSentinel.Builder();
        private RedisServer.Builder serverBuilder = new RedisServer.Builder();
        private int sentinelCount = 1;
        private int quorumSize = 1;
        private PortProvider sentinelPortProvider = new SequencePortProvider(26379);
        private PortProvider replicationGroupPortProvider = new SequencePortProvider(6379);
        private final List<ReplicationGroup> groups = new LinkedList<>();

        public Builder withSentinelBuilder(RedisSentinel.Builder sentinelBuilder) {
            this.sentinelBuilder = sentinelBuilder;
            return this;
        }

        public Builder withServerBuilder(RedisServer.Builder serverBuilder) {
            this.serverBuilder = serverBuilder;
            return this;
        }

        public Builder sentinelPorts(Collection<Integer> ports) {
            this.sentinelPortProvider = new PredefinedPortProvider(ports);
            this.sentinelCount = ports.size();
            return this;
        }

        public Builder serverPorts(Collection<Integer> ports) {
            this.replicationGroupPortProvider = new PredefinedPortProvider(ports);
            return this;
        }

        public Builder ephemeralSentinels() {
            this.sentinelPortProvider = new EphemeralPortProvider();
            return this;
        }

        public Builder ephemeralServers() {
            this.replicationGroupPortProvider = new EphemeralPortProvider();
            return this;
        }


        public Builder ephemeral() {
            ephemeralSentinels();
            ephemeralServers();
            return this;
        }

        public Builder sentinelCount(int sentinelCount) {
            this.sentinelCount = sentinelCount;
            return this;
        }

        public Builder sentinelStartingPort(int startingPort) {
            this.sentinelPortProvider = new SequencePortProvider(startingPort);
            return this;
        }

        public Builder quorumSize(int quorumSize) {
            this.quorumSize = quorumSize;
            return this;
        }

        public Builder replicationGroup(String masterName, int slaveCount) {
            this.groups.add(new ReplicationGroup(masterName, slaveCount, this.replicationGroupPortProvider));
            return this;
        }

        public Builder logProcessOutput() {
            this.serverBuilder.logProcessOutput();
            this.sentinelBuilder.logProcessOutput();
            return this;
        }

        public Builder startupTimeoutMs(long startupTimeoutMs) {
            this.serverBuilder.startupTimeoutMs(startupTimeoutMs);
            this.sentinelBuilder.startupTimeoutMs(startupTimeoutMs);
            return this;
        }


        public SentinelCluster build() {
            final List<Redis> sentinels = buildSentinels();
            final List<Redis> servers = buildServers();
            return new SentinelCluster(sentinels, servers);
        }

        private List<Redis> buildServers() {
            List<Redis> servers = new ArrayList<>();
            for (ReplicationGroup g : groups) {
                servers.add(buildMaster(g));
                buildSlaves(servers, g);
            }
            return servers;
        }

        private void buildSlaves(List<Redis> servers, ReplicationGroup g) {
            for (Integer slavePort : g.slavePorts) {
                serverBuilder.reset();
                serverBuilder.port(slavePort);
                serverBuilder.slaveOf("localhost", g.masterPort);
                final RedisServer slave = serverBuilder.build();
                servers.add(slave);
            }
        }

        private Redis buildMaster(ReplicationGroup g) {
            serverBuilder.reset();
            return serverBuilder.port(g.masterPort).build();
        }

        private List<Redis> buildSentinels() {
            int toBuild = this.sentinelCount;
            final List<Redis> sentinels = new LinkedList<>();
            while (toBuild-- > 0) {
                sentinels.add(buildSentinel());
            }
            return sentinels;
        }

        private Redis buildSentinel() {
            sentinelBuilder.reset();
            sentinelBuilder.port(nextSentinelPort());
            for (ReplicationGroup g : groups) {
                sentinelBuilder.masterName(g.masterName);
                sentinelBuilder.masterPort(g.masterPort);
                sentinelBuilder.quorumSize(quorumSize);
                sentinelBuilder.addDefaultReplicationGroup();
            }
            return sentinelBuilder.build();
        }

        private int nextSentinelPort() {
            return sentinelPortProvider.next();
        }

        private static class ReplicationGroup {
            private final String masterName;
            private final int masterPort;
            private final List<Integer> slavePorts = new LinkedList<>();

            private ReplicationGroup(String masterName, int slaveCount, PortProvider portProvider) {
                this.masterName = masterName;
                masterPort = portProvider.next();
                while (slaveCount-- > 0) {
                    slavePorts.add(portProvider.next());
                }
            }
        }
    }
}
