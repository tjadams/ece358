package lab1;

import java.util.ArrayList;
import java.util.Scanner;

// Note that this class is a singleton so we'll use global variables
public class simulator {
    // TODO make sure all ticks are longs
    static boolean is_MD1;
    // Total number of ticks for the simulation (ticks)
    static int num_of_ticks;
    // Average number of packets generated/arrived (packets/sec). This is lambda in the lab notes
    static int packet_gen_rate;
    // Packet size (bits)
    static int packet_size;
    // Utilization of the queue (packets). This is p in the lab notes
    static int q_utilizaton;
    // Duration of the simulation (sec)
    static double simul_duration;
    // Current tick (ticks)
    static long t;
    // Arrival time of a packet (ticks)
    static long t_arrival;
    // Departure time of a packet (ticks)
    static long t_departure;
    // Transmission time (ticks)
    static long t_transmission;
    // Duration of a single tick (sec)
    static double tick_duration;
    // The number of times experiements are repeated
    static int M;
    // Check queue size every t_queue_check number of ticks (used for E_N calculations)
    static int t_queue_check;
    // Counter modulo t_queue_check that counts number of ticks
    static int t_queue_check_ctr;
    // An instance of the size of the MD1 or MD1K queue
    static int queue_size;
    // List that contains instances of the size of the respective MD1 or MD1K queue every t_queue_check amount of ticks
    static ArrayList<Integer> queue_size_list;
    // List that contains the sojourn amount of ticks of each packet (used for the E_T calculation)
    static ArrayList<Integer> sojourn_list;
    static int packets_lost;
    static int packets_generated;
    // Outputs (array size is M)
    static int E_N[];
    static int E_T[];
    static int P_LOSS[];
    static int P_IDLE[];

    static MD1Queue md1Queue;
    static MD1KQueue md1KQueue;
    static Scanner scanner;

    public static void main(String args[]) {
        initialize_variables();

        t_arrival = calc_arrival_time(); // calculate first packet arrival time
        // TODO wrap these 2 for loops and create_report() in another for loop for p. Think about if i need to reset variables and ticks for p etc
        for (int j = 0; j < M; j++) {
            for (int i = 1; i <= num_of_ticks; i++) {
                // E_N intermediate calculations
                t_queue_check_ctr++;

                arrival();
                departure();

                if (t_queue_check_ctr % t_queue_check == 0) {
                    t_queue_check_ctr = 0;
                    queue_size_list.add(queue_size);
                }
            }
            calculate_E_N(j);
            calculate_E_T(j);
            // TODO calculate other outputs
            if (!is_MD1) {
                calculate_P_LOSS(j);
            }
        }
        create_report();
    }

    public static void initialize_variables() {
        final int MS_PER_SEC = 1000;
        final int SEC_PER_MIN = 60;

        M = 5;

        E_N = new int [M];
        E_T = new int [M];
        P_IDLE = new int [M];
        P_LOSS = new int [M];

        // Check every 5 ticks
        t_queue_check = 5;

        t_queue_check_ctr = 0;
        queue_size_list = new ArrayList<>();
        packets_lost = 0;
        packets_generated = 0;

        // Ask for inputs
        scanner = new Scanner(System.in);
        System.out.println("Hello! Welcome to the simulation.");

        // Receive ms, store in seconds.
        System.out.println("Duration for each tick (in ms) <= 1 ms: ");
        // (sec) = (ms) / (1000 ms / sec)
        tick_duration = (double) scanner.nextInt() / MS_PER_SEC;

        // Receive minutes, store in seconds.
        System.out.println("Simulation time (in minutes): ");
        // (min) = (min) * (60 sec / min)
        simul_duration = (double) scanner.nextInt() * SEC_PER_MIN;

        // (ticks) = (sec) / (sec / tick)
        num_of_ticks = (int) Math.ceil(simul_duration / tick_duration);


        System.out.println(
            "\u03BB, Average number of packets generated/arrived  (packets/sec): "
        );
        packet_gen_rate = scanner.nextInt();

        System.out.println("L, Length of a packet (bits): ");
        packet_size = scanner.nextInt();

        System.out.println("C, Transmission rate (bits/sec): ");
        final int transmission_rate = scanner.nextInt();

        // (ticks) = ((bits) / (bits / sec)) / (sec / tick)
        t_transmission = (int) Math.ceil(
            (packet_size / transmission_rate) / tick_duration
        );

        q_utilizaton = packet_size * (packet_gen_rate / transmission_rate);

        pick_a_queue();
    }

    public static void arrival() {
        if (t >= t_arrival) {
            KendallPacket new_packet = new KendallPacket(packet_size);
            // TODO confirm that this is the correct tick or if it should be t_arrival
            new_packet.setT_generate(t);

            if (is_MD1) {
                md1Queue.add(new_packet);
                queue_size = md1Queue.getSize();
                packets_generated++;
            } else {
                boolean isAddSuccessful = md1KQueue.add(new_packet);
                queue_size = md1KQueue.getSize();
                if (!isAddSuccessful) {
                    packets_lost++;
                } else {
                    packets_generated++;
                }
            }

            t_arrival = t + calc_arrival_time();
            t_departure = t + t_transmission;
        }
    }

    public static void departure() {
        if (t >= t_departure) {
            KendallPacket departed_packet;
            if (is_MD1) {
                departed_packet = md1Queue.remove();
            } else {
                departed_packet = md1KQueue.remove();
            }
            departed_packet.setT_finished(t);
            sojourn_list.add(departed_packet.getSojournAmountOfTicks());
        }
    }

    public static long calc_arrival_time() {
        double u = Math.random(); // random number between 0 and 1
        double arrival_time =
            ((-1 / packet_gen_rate) * Math.log(1 - u)) / tick_duration;

        return (long) Math.ceil(arrival_time);
    }

    public static void pick_a_queue() {
        final String MD1 = "y";
        final String MD1K = "n";
        final String msg = "M/D/1 queue (y) or a M/D/1/K queue (n)? (y/n)";

        System.out.println(msg);
        String choice = scanner.next();

        while (!(choice.equals(MD1) || choice.equals(MD1K))) {
            System.out.println(msg);
            choice = scanner.next();
        }

        if (choice.equals(MD1)) {
            is_MD1 = true;
            System.out.println("M/D/1 queue selected.");

            md1Queue = new MD1Queue();
        } else {
            is_MD1 = false;
            System.out.println("M/D/1/K queue selected.");

            System.out.println("Please enter an integer value for K, the buffer size of the queue: ");
            int K = scanner.nextInt();

            md1KQueue = new MD1KQueue(K);
        }

        scanner.close();
    }

    // Display outputs
    public static void create_report() {
        for (int i = 0; i < M; i++) {
            System.out.println("E{N] for M-1= " + i + " is: " + E_N[i]);
            System.out.println("E{T] for M-1 = " + i + " is: " + E_T[i]);
            System.out.println("P_IDLE for M-1 = " + i + " is: " + P_IDLE[i]);
            if (!is_MD1) {
                System.out.println("P_LOSS for M-1 = " + i + " is: " + P_LOSS[i]);
            }
        }
    }

    public static void calculate_E_N(int j) {
        long sum = 0;
        for (int i = 0; i < queue_size_list.size(); i++) {
            sum = sum + queue_size_list.get(i);
        }

        E_N[j] = sum/queue_size_list.size();

        // Reset variables that will be used in future calculations
        queue_size_list.clear();
    }

    public static void calculate_P_LOSS(int j) {
        P_LOSS[j] = packets_lost/packets_generated;

        // Reset variables that will be used in future calculations
        packets_lost = 0;
        packets_generated = 0;
    }

    public static void calculate_E_T(int j) {
        long average_sojourn_timeasdfads
                asdfas
    }
}