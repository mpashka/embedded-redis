package redis.embedded.cluster;

enum ClusterState {
    OK("ok"), FAIL("fail");

    private String state;

    ClusterState(String s) {
        state = s;
    }

    public String getState() {
        return state;
    }

    public static ClusterState getStateByStr(String s) {
        for (ClusterState clusterState : ClusterState.values()) {
            if (s.equals(clusterState.getState())) {
                return clusterState;
            }
        }
        throw new IllegalStateException("illegal cluster state: " + s);
    }
}
