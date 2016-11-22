package lab2;

public class Node {
    // TODO add a queue
    int uniqueId;
    int state;
    boolean sensingMedium;
    boolean is_waiting_retransmit = false;
    int state_start_tick;
    int state_end_tick;
    int i;
    boolean isTransmitting = false;
    boolean is_random_waiting = false;
    int t_doneTransmitting;
    MD1Queue queue;

    public Node(int uniqueId) {
        // TODO initialize queue
        this.state = 0;
        this.sensingMedium = false;
        this.state_start_tick = -1;
        this.state_end_tick = -1;
        this.i = 0;
        this.uniqueId = uniqueId;
        this.queue = new MD1Queue();
    }
}
