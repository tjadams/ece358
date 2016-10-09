import lab1.KendallPacket;

import java.util.ArrayDeque;
import java.util.Queue;

public class KendallQueue {

    private Queue<KendallPacket> q;

    public KendallQueue(int numPackets) {
        this.q = new ArrayDeque<>(numPackets);
    }
}
