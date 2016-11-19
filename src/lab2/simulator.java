package lab2;

public class simulator {
    static Node node;
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

            }





            // TODO should I determine state at beginning of function or determine next state at end of function or both?
            // Determine state as a function of time elapsed (ticks) (bittime =  nanoseconds or microseconds or what?)

            //
            //          }
        }
    }

    public static void initialize_variables() {
        node = new Node();
    }
}