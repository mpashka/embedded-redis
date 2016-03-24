package redis.embedded.cluster;

class SlotRange {
    private final int first;
    private final int last;

    SlotRange(int first, int last) {
        this.first = first;
        this.last = last;
    }

    int[] getRange() {
        int[] range = new int[last - first + 1];
        for (int i = 0; i <= last - first; i++) {
            range[i] = first + i;
        }
        return range;
    }

    @Override
    public String toString() {
        return "[" + first + ", " + last + "]";
    }
}
