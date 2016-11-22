package lab2;

// Note that this class is a singleton so we'll use global variables
// ASSUMPTIONS:
// Use Bus topology
// Distance between adjacent neighbours are the same but since it's bus topology, distance from one node to the other is abs(node 1s number - node 2s number)*distanceBetweenTwoNeighbours
// Queue size is infinite
// Distance between two neighbours is 1 metre

import java.util.concurrent.ThreadLocalRandom;

public class Simulator {
    static Node[] nodes;
    static double probability_p = 0.1;
    static double distance = 1.0;
    static double total_packets = 0.0;
    static double dropped_packets = 0.0;
    static double num_of_ticks = 50000000.0;
    static double seconds_per_nanosecond = 1.0/1000000000.0;
    static double seconds_per_tick = 10.0*seconds_per_nanosecond; // 1 tick in seconds = seconds per tick = tick duration
    static double ticks_in_one_second = 1.0/seconds_per_tick;
    static double megabits_per_second = 1000000.0;
    static int N = 20;
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
    static long cumulativeDelay = 0;
    static int csma_cd_type = 1;    // 1: non-persistent, 2: p-persistent
    // Arrival time of a packet (ticks)
    static int t_arrival;
    // Departure time of a packet (ticks)
    static int t_departure;

    static int t_propogation; // propogation time in ticks between adjacent two nodes
    static int t_transmission = (int)(packet_service_time*ticks_in_one_second);

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
            for (int i = 0; i < N; i++) {
                if (randomNode.state == 0) {
                    randomNode.i = 0;
                    senseMedium(randomNode, t);
                } else if (randomNode.state == 1) {
                    senseMedium(randomNode, t);
                }
                // Note that state 2 and state 3 from the diagram are merged into state 2
                else if (randomNode.state == 2) {
                    switch(csma_cd_type) {
                        case 1: // Non-persistent
                            non_persistent(randomNode, t);
                            break;
                        case 2: // P-persistent
                            p_persistent(randomNode, t);
                            break;
                    }
                } else if (randomNode.state == 4) {
                    binary_exp_backoff(randomNode, t);
                }
            }
        }
        create_report();
    }

    public static Node findARandomNode() {
        int random = ThreadLocalRandom.current().nextInt(0, nodes.length);
        return nodes[random];
    }

    public static Node findARandomNodeThatIsnt(Node node) {
        int random = ThreadLocalRandom.current().nextInt(0, nodes.length);
        if (nodes[random].uniqueId == node.uniqueId) {
            if (random > 0 && random < N) {
                return nodes[random - 1];
            } else if (random == 0) {
                return nodes[random + 1];
            }
        }
        return nodes[random];
    }

    private static void initialize_variables() {
        nodes = new Node[N];
        for (int i = 0; i < N; i++) {
            nodes[i] = new Node(i);
        }

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
            receiveTransmit(fromNode, toNode, t);

            // "while detecting collision"
            if (isCollision(fromNode)) {
                fromNode.state = 4;
            } else {
                // upgrade to state 1
                fromNode.state = 1;
            }
        } else {
            // Random wait
            if (!fromNode.is_random_waiting) {
                fromNode.is_random_waiting = true;
                double random_num = ThreadLocalRandom.current()
                        .nextInt(0, (int) Math.pow(2, (double) 4.0));
                fromNode.state_start_tick = t;
                fromNode.state_end_tick = t + (int) bitTimeToTicks(Tp * random_num);
            } else {
                if (t > fromNode.state_end_tick) {
                    fromNode.is_random_waiting = false;
                    fromNode.state = 1;
                    resetNodeTiming(fromNode);
                }
            }
        }
    }

    public static boolean isCollision(Node A) {
        for (int i = 0; i < nodes.length; i++) {
            Node B = nodes[i];
            if (A.uniqueId != B.uniqueId && A.isTransmitting &&
                    B.isTransmitting && withinTimeFrame(A, B)){
                return true;
            }
        }

        return false;
    }

    public static boolean withinTimeFrame (Node A, Node B) {
        if (B.state_start_tick > A.state_start_tick &&
                B.state_end_tick < A.state_end_tick) {
                return true;
        }
        return false;
    }
    
    public static void p_persistent(Node fromNode, int t) {
        if (!is_medium_busy(fromNode)) {
            double probability_result = Math.random();

            if (probability_result > probability_p) {
                // Random wait
                if (!fromNode.is_random_waiting) {
                    fromNode.is_random_waiting = true;
                    double random_num = ThreadLocalRandom.current()
                            .nextInt(0, (int) Math.pow(2, (double) 4.0));
                    fromNode.state_start_tick = t;
                    fromNode.state_end_tick = t + (int) bitTimeToTicks(Tp * random_num);
                } else {
                    if (t > fromNode.state_end_tick) {
                        fromNode.is_random_waiting = false;
                        fromNode.state = 1;
                        resetNodeTiming(fromNode);
                    }
                }

                if (is_medium_busy(fromNode)) {
                    binary_exp_backoff(fromNode, t);
                } else {
                    // Try sensing medium again
                    fromNode.state = 1;
                }
            } else {
                // Successful probability outcome

                // One attempt to transmit per tick (this is called once per tick)
                // but a transmission can collide with ongoing transmissions
                Node toNode = findARandomNodeThatIsnt(fromNode);
                transmit(fromNode, toNode, t);
                receiveTransmit(fromNode, toNode, t);

                // "while detecting collision"
                if (isCollision(fromNode)) {
                    fromNode.state = 4;
                } else {
                    // upgrade to state 1
                    fromNode.state = 1;
                }
            }
        } else {
            fromNode.state = 1;
        }
    }

    // If any node other than "transmitting" is transmitting
    // then the medium is busy
    public static boolean is_medium_busy(Node transmitting) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (node.isTransmitting && (node.uniqueId != transmitting.uniqueId)) {
                return true;
            }
        }
        return false;
    }

    public static void transmit(Node fromNode, Node toNode, int t) {
        if (t >= t_departure && !fromNode.isTransmitting) {
            fromNode.isTransmitting = true;
            fromNode.state_start_tick = t;
            fromNode.queue.remove();
            double numHopsAway = Math.abs(toNode.uniqueId - fromNode.uniqueId);
            fromNode.t_doneTransmitting = (int)(t + t_propogation*numHopsAway);
            cumulativeDelay += t_propogation*numHopsAway;
        }
    }

    public static void receiveTransmit(Node fromNode, Node toNode, int t) {
        if (t >= fromNode.t_doneTransmitting && fromNode.isTransmitting) {
            fromNode.isTransmitting = false;
            fromNode.state_end_tick = t;
            // Add a packet to toNode queue. We don't have to add the same
            // packet we popped because all packets are basically the same
            toNode.queue.add(new KendallPacket((int)L));
            // Note that upgrading to the state is done in the collision detection
        }
    }

    // Arrival of packet from upper layer
    public static void arrival(Node node, int t) {
        if (t >= t_arrival) {
            total_packets++;
            t_departure = t + t_transmission;

            KendallPacket new_packet = new KendallPacket((int)L);

            node.queue.add(new_packet);

            t_arrival = t + calc_arrival_time();
        }
    }

    public static void departure(Node node, int t) {
        if (t >= t_departure) {
            if (node.queue.getSize() != 0) {
                node.queue.remove();
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
        System.out.println("Throughput: " + throughput);

        double delay = cumulativeDelay/M;
        System.out.println("Delay: " + delay);
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