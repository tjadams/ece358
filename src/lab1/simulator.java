package lab1;

import java.util.Scanner;

// Note that this class is a singleton so we'll use global variables
public class simulator {
    static boolean is_MD1;
    // Total number of ticks for the simulation (ticks)
    static int num_of_ticks;
    // Average number of packets generated/arrived (packets/sec)
    static int packet_gen_rate;
    // Packet size (bits)
    static int packet_size;
    // Utilization of the queue (packets)
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

    // TODO create this class, a superclass of MD1 and MD1K queues
    static KendallQueue queue;
    static Scanner scanner;

    public static void main(String args[]) {
        initialize_variables();

        t_arrival = calc_arrival_time(); // calculate first packet arrival time
        // TODO appendix B says to repeat this for loop like 5 times or something...
        for (int i = 1; i <= num_of_ticks; i++) {
            arrival();
            departure();
        }
        create_report();
    }

    public static void initialize_variables() {
        final int MS_PER_SEC = 1000;
        final int SEC_PER_MIN = 60;

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

            queue.add(new_packet);

            t_arrival = t + calc_arrival_time();
            t_departure = t + t_transmission;
            // TODO add support for the packet loss case of MD1K where you can't add to the queue. Maybe do something like have queue.add() return a boolean representing success. If it's false then increase number of packets lost by 1. This would only be false in the situation that you have a MD1K queue and that queue is full and you try to add a packet to it
        }
    }

    public static void departure() {
        if (t >= t_departure) {
            queue.pop();
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
            // TODO set queue equal to a new md1 queue
        } else {
            is_MD1 = false;
            System.out.println("M/D/1/K queue selected.");

            System.out.println("Please enter an integer value for K, the buffer size of the queue: ");
            int K = scanner.nextInt();

            // TODO set queue equal to a new md1k queue
        }

        scanner.close();
    }

    public static void create_report() {
        if (is_MD1) {
            // TODO calculate and display outputs of MDOne
            int E_N;
            int E_T;
            int P_IDLE;
        } else {
            // TODO calculate and display outputs of MDOneK
            int E_N;
            int E_T;
            int P_IDLE;
            int P_LOSS;
        }
    }
}