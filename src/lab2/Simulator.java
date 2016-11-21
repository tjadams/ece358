package lab2;

// Note that this class is a singleton so we'll use global variables
// ASSUMPTIONS:
// Use Bus topology
// Distance between adjacent neighbours are the same but since it's bus topology, distance from one node to the other is abs(node 1s number - node 2s number)*distanceBetweenTwoNeighbours
// Queue size is infinite
// Distance between two neighbours is 1 metre

import lab1.KendallPacket;

import java.util.concurrent.ThreadLocalRandom;

public class Simulator {
    static double distance = 1.0;
    static double total_packets = 0.0;
    static double dropped_packets = 0.0;
    static double kMax = 10.0;
    static double num_of_ticks = 2500000.0; // this number is from lab1
    static double seconds_per_nanosecond = 1.0/1000000000.0;
    static double seconds_per_tick = 10.0*seconds_per_nanosecond; // 1 tick in seconds = seconds per tick = tick duration
    static double ticks_in_one_second = 1.0/seconds_per_tick;
    static double megabits_per_second = 1000000.0;
    static double W = 10.0*megabits_per_second; // bits per second TODO could be a multiplier of 10 or 100 for 10 or 100 Mbps
    static double A = 10.0; // packet arrival rate in packets/sec TODO A could be 10 or 20 or 50
    static double L = 1500.0; // packet length is 1500 bytes
    static double lambda = A;
    static double bits_per_byte = 8;
    static double packet_service_time = L*bits_per_byte/W; // (bytes*bits/bytes)/bits per seconds = seconds
    static double S = 2.0*(100000000.0); // signal propogation speed in medium m/s
    static double Tp = 512.0/W; // 512 bits / bits per second = seconds of propogation delay
    static double M = 0; // total number of packets successfully received
    static double medium_sense_bit_time = 96.0;
    static double seconds_per_bit_time = 1.0/W; // Note that bit time means time to transfer one bit = 1 bit / bps = 1/W
    static int csma_cd_type = 1;    // 1: non-persistent, 2: p-persistent
    // Arrival time of a packet (ticks)
    static int t_arrival;
    // Departure time of a packet (ticks)
    static int t_departure;

    static int t_propogation; // propogation time in ticks between adjacent two nodes

    public static void main (String args[]) {
        initialize_variables();

        // Do something each tick for each queue
        for (int t = 1; t <= num_of_ticks; t++) {
            // For every single tick, arrival and departure only happens once
            // A random node receives a packet from the upper layer
            Node randomNode = findARandomNode();
            arrival(randomNode, t);
            departure(randomNode, t);

            // Determine state of each node, do something at that state, upgrade node to next state
            // TODO transform the function into working on multiple nodes(2 nodes and then n nodes) once it works for one node
//            for (int i = 0; i < num_of_nodes; i++) {
            if (randomNode.state == 0) {
                randomNode.i = 0;
                senseMedium(randomNode, t);
            } else if (randomNode.state == 1) {
                senseMedium(randomNode, t);
            } else if (randomNode.state == 2) {
                switch(csma_cd_type) {
                    case 1: // Non-persistent
                        non_persistent(randomNode, t);
                        break;
                    case 2: // P-persistent
                        p_persistent(randomNode, t);
                        break;
                }
            } else if (randomNode.state == 3) {

            } else if (randomNode.state == 4) {
                binary_exp_backoff(randomNode, t);
            }
            //          }
        }
        create_report();
    }

    private static void initialize_variables() {
        // TODO init queues

        // Get first packet arrival time and departure time
        t_arrival = calc_arrival_time();
        t_departure = t_arrival + t_transmission;
        t_propogation = (int)(ticks_in_one_second*(1.0/S)*distance);
    }

    public static int calc_arrival_time() {
        double u = Math.random(); // random number between 0 and 1
        double arrival_time =
                (((double)-1 / (double)lambda) * (double)Math.log(1 - u) * (double)ticks_in_one_second);

        return (int) Math.ceil(arrival_time);
    }

    public static void senseMedium(Node node, int t){
        if (node == null || t < 1) {
            System.err.println("Invalid input to senseMedium");
        }

        if (!node.sensingMedium) {
            node.sensingMedium = true;
            node.state_start_tick = t;
            node.state_end_tick = t + (int)bitTimeToTicks(medium_sense_bit_time);
        } else {
            // If done sensing medium
            if (t > node.state_end_tick) {
                node.sensingMedium = false;
                node.state = 2; // upgrade to next state (state 0 goes to 2 and state 1 goes to 2)
                resetNodeTiming(node);
            }
        }
    }

    public static void non_persistent(Node fromNode, int t) {
        if (!is_medium_busy(fromNode)) {
            // One attempt to transmit per tick (this is called once per tick)
            // but a transmission can collide with ongoing transmissions
            Node toNode = findARandomNodeThatIsnt(fromNode);
            transmit(fromNode, toNode, t);
            receiveTransmit(toNode, t);

            // "while detecting collision"
            if (isCollision(fromNode)) {
                // TODO send jamming signal and abort
                // TODO upgrade to state 4
            } else {
                // upgrade to state 1
                fromNode.state = 1;
            }
        } else {
            // TODO random wait
            randomWait before sensing medium again
        }
    }

    public static boolean isCollision(Node A) {
        // TODO loop through every node (node B as in slide 15 from the implementation)
        if (A.isTransmitting && B.isTransmitting && withinTimeFrame(A, B)){
            return true;
        }

        // outside of for loop
        return false;
    }

    public static boolean withinTimeFrame (Node A, Node B) {
        if (B.state_start_tick > A.state_start_tick &&
                B.state_end_tick < A.state_end_tick) {
                return true;
            }
        }
    }

    // TODO implement p persistent
    public static void p_persistent(Node fromNode, int t) {
    }

    // If any node other than "transmitting" is transmitting
    // then the medium is busy
    public static boolean is_medium_busy(Node transmitting){
        // TODO do a linear search on every node and stop when you find one that's busy
        if (node.isTransmitting && (node.uniqueId != transmitting.uniqueId)) {
            return true;
        }

        // Outside of for loop
        return false;
    }

    public static void transmit(Node fromNode, Node toNode, int t) {
        if (t >= t_departure && !fromNode.isTransmitting) {
            fromNode.isTransmitting = true;
            fromNode.queue.pop();
            // TODO determine from the queues by using absolute value
            double numHopsAway = 1.0;
            t_doneTransmitting = t + t_propogation*numHopsAway;
        }
    }

    public static void receiveTransmit(Node fromNode, Node toNode, int t) {
        if (t >= t_doneTransmitting && fromNode.isTransmitting) {
            // TODO have to transmit frame while detecting collision
            fromNode.isTransmitting = false;
            // Add a packet to toNode queue. We don't have to add the same
            // packet we popped because all packets are basically the same
            toNode.queue.add(new KendallPacket(L));
            // TODO upgrade transmitter to next state
            fromNode.state = ...?;
        }
    }

    // Arrival of packet from upper layer
    // Receive from one node and send to all others
    public static void arrival(Node node, int t) {
        if (t >= t_arrival) {
            total_packets++;
            t_departure = t + t_transmission;

            KendallPacket new_packet = new KendallPacket((int)L);

            // Transmit to ONE random node other than this one
            md1Queue.add(new_packet);
            queue_size = md1Queue.getSize();

            t_arrival = t + calc_arrival_time();
        }
    }

    public static void departure(int t) {
        if (t >= t_departure) {
            if (md1Queue.getSize() != 0) {
                md1Queue.remove();
            }
        }
    }

    public static void binary_exp_backoff(Node node, int t) {
        final int max_retransmit_count = 10;
        // Tp = 512 / W. Equivalent to 512 bit time.
        final double Tp = 512;
        int random_num;
        double Tb;

        if (node.i > max_retransmit_count) {
            // Drop packet
            dropped_packets++;
            node.state = 0;
        } else {
            if (!node.is_waiting_retransmit) {
                node.i = node.i + 1;
                node.is_waiting_retransmit = true;
                // Generate random number between 0 and 2^i - 1
                // http://stackoverflow.com/a/363692
                random_num = ThreadLocalRandom.current()
                        .nextInt(0, (int) Math.pow(2, (double) node.i));
                Tb = Tp * random_num;
                node.state_start_tick = t;
                node.state_end_tick = t + (int) bitTimeToTicks(Tb);
            } else {
                if (t > node.state_end_tick) {
                    node.is_waiting_retransmit = false;
                    // Go back to state 0 and sense the medium.
                    node.state = 1;
                    resetNodeTiming(node);
                }
            }
        }
    }

    public static void create_report() {
        long simulation_time = (long)(num_of_ticks*seconds_per_tick); // (ticks*seocnds/tick in seconds)
        M = total_packets - dropped_packets;
        double throughput = M*L*bits_per_byte/simulation_time; // bits/seconds

        // TODO delay

        // TODO question 2.
    }

    public static double bitTimeToTicks (double bit_time) {
        double result = bit_time*seconds_per_bit_time/seconds_per_tick;
        return Math.ceil(result); // double result gives something like 959.999 when we want 960
    }

    // Set node timing to nonsense values because we're no longer timing this node
    // Note that this isn't necessary but it could help for debugging
    public static void resetNodeTiming(Node node) {
        node.state_start_tick = -1;
        node.state_end_tick = -1;
    }
}