package lab2;

public class Node {
    // TODO add a queue
    static int state;
    static boolean sensingMedium;
    static long state_start_tick;
    static long state_end_tick;
    public Node node() {
        // TODO initialize queue
        this.state = 0;
        this.sensingMedium = false;
    }
}
