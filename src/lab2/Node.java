package lab2;

public class Node {
    // TODO add a queue
    int state;
    boolean sensingMedium;
    int state_start_tick;
    int state_end_tick;
    int i;
    public Node() {
        // TODO initialize queue
        this.state = 0;
        this.sensingMedium = false;
        this.state_start_tick = -1;
        this.state_end_tick = -1;
        this.i = 0;
    }
}
