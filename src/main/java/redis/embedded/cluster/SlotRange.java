package redis.embedded.cluster;

class SlotRange {
    final int first;
    final int last;

    SlotRange(int first, int last) {
        this.first = first;
        this.last = last;
    }

    public int[] getRange() {
        int[] range = new int[last - first + 1];
        for (int i = 0; i <= last - first; i++) {
            range[i] = first + i;
        }
        return range;
    }
}
