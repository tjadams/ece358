package lab2;

import java.util.concurrent.ThreadLocalRandom;

public class simulator {
    static lab2.node node;
    static boolean medium_busy;

    public static void main (String args[]) {
        // Do something each tick for each queue
        initialize_variables();

        for (int t = 1; t <= num_of_ticks; t++) {
            // Determine state of each queue and do something with it
            // TODO transform the function into working on multiple nodes(2 nodes and then n nodes) once it works for one node
//            for (int i = 0; i < num_of_nodes; i++) {
            if (node.state == 0) {
                node.i = 0;
                senseMedium();
                if (medium_busy) {
                    wait();
                } else {
                    transmit();
                }
            }





            // TODO should I determine state at beginning of function or determine next state at end of function or both?
            // Determine state as a function of time elapsed (ticks) (bittime =  nanoseconds or microseconds or what?)

            //
            //          }
        }
    }

    public static void initialize_variables() {
        node = new node();
    }

    public static void transmit(lab2.node node) {
        // TODO: Detect collision
        node.remove();
        // Does this packet go to another node or somewhere?

    }

    public static void binary_exp_backoff(lab2.node node) {
        int max_retransmit_count = 10;
        int random_num = 0;
        int Tp = 512;
        int Tb = 0;

        node.increment_retransmit_count();
        if (node.i > max_retransmit_count) {
            // Drop packet
        } else {
            // Generate random number between 0 and 2^i - 1
            // http://stackoverflow.com/a/363692
            random_num = ThreadLocalRandom.current()
                    .nextInt(0, (int) Math.pow(2, (double) node.i));

            Tb = Tp * random_num;

            // Wait(Tb)

        }
    }
}