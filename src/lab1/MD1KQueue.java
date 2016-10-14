package lab1;

import java.util.LinkedList;
import java.util.Queue;

public class MD1KQueue {
    private Queue<KendallPacket> q;
    private int K;

    public MD1KQueue(int K) {
        this.q = new LinkedList<>();
        this.K = K;
    }

    // Returns a boolean representing success or failure for adding an item to the queue
    public boolean add(KendallPacket packet) {
        if (q.size() < K) {
            q.add(packet);
            return true;
        } else {
            return false;
        }
    }

    public int getSize() { return q.size(); }

    public int getK() { return K; }

    public KendallPacket remove() {
        return q.remove();
    }
}
