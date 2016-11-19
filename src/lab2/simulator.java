package lab2;

// Note that this class is a singleton so we'll use global variables
// ASSUMPTIONS:
// Use Bus topology
// Distance between adjacent neighbours are the same but since it's bus topology, distance from one node to the other is abs(node 1s number - node 2s number)*distanceBetweenTwoNeighbours

// QUESTIONS:
// TODO what should we assume the distance between two neighbours is?
// TODO is queue size finite or infinite?

public class simulator {
    static Node node;
    static double kMax = 10;
    static double num_of_ticks = 2500000; // this number is from lab1
    static double seconds_per_nanosecond = 1/1000000000;
    static double seconds_per_tick = 10*seconds_per_nanosecond; // 1 tick in seconds = seconds per tick = tick duration
    static double megabits_per_second = 1000000;
    static double W = 10*megabits_per_second; // bits per second TODO could be a multiplier of 10 or 100 for 10 or 100 Mbps
    static double A = 10; // packet arrival rate in packets/sec TODO A could be 10 or 20 or 50
    static double L = 1500; // packet length is 1500 bytes
    static double bits_per_byte = 8;
    static double packet_service_time = L*bits_per_byte/W; // (bytes*bits/bytes)/bits per seconds = seconds
    static double S = 2*(100000000); // signal propogation speed in medium m/s
    static double Tp = 512/W; // 512 bits / bits per second = seconds of propogation delay
    static double M = 0; // total number of packets successfully received
    static double medium_sense_bit_time = 96;
    static double seconds_per_bit_time = 1/W; // Note that bit time means time to transfer one bit = 1 bit / bps = 1/W

    public static void main (String args[]) {
        initialize_variables();

        // Do something each tick for each queue
        for (int t = 1; t <= num_of_ticks; t++) {
            // Determine state of each node, do something at that state, upgrade node to next state
            // TODO transform the function into working on multiple nodes(2 nodes and then n nodes) once it works for one node
//            for (int i = 0; i < num_of_nodes; i++) {
            if (node.state == 0) {
                node.i = 0;
                senseMedium(node, t);
            } else if (node.state == 1) {

            } else if (node.state == 2) {

            } else if (node.state == 3) {

            } else if (node.state == 4) {

            }
            //          }
        }
        create_report();
    }

    private static void initialize_variables() {
        // TODO
        node = new Node();
    }

    private static void senseMedium(Node node, int t) {
        if (!node.sensingMedium) {
            node.sensingMedium = true;
            node.state_start_tick = t;
            node.state_end_tick = t + bitTimeToTicks(medium_sense_bit_time);
        } else {
            // If done sensing medium
            if (t > node.state_end_tick) {
                node.sensingMedium = false;
                node.state = 2; // upgrade to next state (state 0 goes to 2 and state 1 goes to 2)
                resetNodeTiming(node);
            }
        }
    }

    private static void create_report() {
        long simulation_time = (long)(num_of_ticks*seconds_per_tick); // (ticks*seocnds/tick in seconds)
        double throughput = M*L*bits_per_byte/simulation_time; // bits/seconds

        // TODO delay

        // TODO question 2.
    }

    private static long bitTimeToTicks (double bit_time) {
        return (long)(bit_time*seconds_per_bit_time/seconds_per_tick);
    }

    // Set node timing to nonsense values because we're no longer timing this node
    // Note that this isn't necessary but it could help for debugging
    private static void resetNodeTiming(Node node) {
        node.state_start_tick = -1;
        node.state_end_tick = -1;
    }
}