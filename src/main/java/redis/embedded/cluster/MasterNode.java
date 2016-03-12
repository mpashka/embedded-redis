package redis.embedded.cluster;

import redis.embedded.*;

import java.util.*;

class MasterNode {
    final Redis master;
    String nodeId;
    final SlotRange slotRange;
    final Set<Redis> slaves;

    public MasterNode(Redis master, SlotRange slotRange) {
        this.master = master;
        this.slotRange = slotRange;
        slaves = new HashSet<>();
    }

    public Set<Redis> getSlaves() {
        return slaves;
    }

    public void addSlave(Redis slave) {
        slaves.add(slave);
    }

    public Redis getMaster() {
        return master;
    }

    public SlotRange getSlotRange() {
        return slotRange;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
