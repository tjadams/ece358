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

    public void add(KendallPacket packet) {
        if (q.size() <= K) {
            q.add(packet);
        }
    }

    public void remove() {
        q.remove();
    }
}
