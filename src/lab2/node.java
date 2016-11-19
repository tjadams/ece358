package lab2;

public class node {
    // TODO add a queue
    int state;
    int i = 0;

    public node() {
        // TODO initialize queue
        this.state = 0;
    }

    public void increment_retransmit_count() {
        i += 1;
    }
}
