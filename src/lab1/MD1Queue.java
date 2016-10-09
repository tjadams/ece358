package lab1;

import java.util.LinkedList;
import java.util.Queue;

public class MD1Queue {
    private Queue<KendallPacket> q;

    public MD1Queue() {
        this.q = new LinkedList<>();
    }

    public void add(KendallPacket packet) {
        q.add(packet);
    }

    public void remove() {
        q.remove();
    }
}
