package lab1;

import java.util.ArrayList;
import java.util.Scanner;

// Note that this class is a singleton so we'll use global variables
// Note that we're saying that ticks are ints so the max number is only like 2 billion/million or so
public class simulator {
    static boolean is_MD1;
    // Total number of ticks for the simulation (ticks)
    static int num_of_ticks;
    // Average number of packets generated/arrived (packet gen rate in packets/sec). This is lambda in the lab notes
    static double lambda;
    // Packet size (bits)
    static int L;
    static int C;
    // Duration of the simulation (sec)
    static double simul_duration;
    // Current tick (ticks)
    static int t;
    // Arrival time of a packet (ticks)
    static int t_arrival;
    // Departure time of a packet (ticks)
    static int t_departure;
    // Transmission time (ticks)
    static int t_transmission;
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
    static int t_idle;
    // Boolean that is true if not doing anything in arrival (no new packets coming in), not doing anything in departure
    // (no old packets leaving). Later combines with a queue is empty check (not servicing packets in queue) to
    // become a boolean that checks if the simulator is idle
    static boolean is_mostly_idle;

    // Represents the rho inequalities as in question 2 and for from the report
    static double p_step_size;
    static double p_start;
    static double p_end;
    static int p_num_steps;

    // Outputs (array size is M)
    static double E_N[][]; // avg # of packets in the queue (# packets)
    static int E_T[][]; // (ms)
    static double P_LOSS[][]; // (%)
    static double P_IDLE[][]; // (%)

    static MD1Queue md1Queue;
    static MD1KQueue md1KQueue;
    static Scanner scanner;

    public static void main(String args[]) {
        initialize_variables();

        t_arrival = calc_arrival_time(); // calculate first packet arrival time
        // TODO Think about if i need to reset variables and ticks for p etc
        int p_index = 0;
        double p_value = p_start;
        while (p_index < p_num_steps) {
            lambda = p_value*C/L;
            p_value += p_step_size;
            for (int j = 0; j < M; j++) {
                for (int i = 1; i <= num_of_ticks; i++) {
                    // intermediate calculations for outputs
                    t_queue_check_ctr++;
                    is_mostly_idle = true;

                    // simulate
                    arrival();
                    departure();

                    // more intermediate calculations for outputs
                    boolean is_idle = is_mostly_idle && md1KQueue.getSize() == 0 && md1Queue.getSize() == 0;
                    if (is_idle) {
                        t_idle++;
                    }

                    if (t_queue_check_ctr % t_queue_check == 0) {
                        t_queue_check_ctr = 0;
                        queue_size_list.add(queue_size);
                    }
                }

                calculate_E_N(j, p_index);
                calculate_E_T(j, p_index);
                calculate_P_IDLE(j, p_index);
                if (!is_MD1) {
                    calculate_P_LOSS(j, p_index);
                }
            }
            create_report(p_index);
            p_index++;
        }
    }

    public static void initialize_variables() {
        final int MS_PER_SEC = 1000;
        final int SEC_PER_MIN = 60;

        M = 5;

        p_step_size = 0.1;

        // Check every 5 ticks
        t_queue_check = 5;

        t_queue_check_ctr = 0;
        queue_size_list = new ArrayList<>();
        packets_lost = 0;
        packets_generated = 0;
        sojourn_list = new ArrayList<>();
        t_idle = 0;

        // Ask for inputs
        scanner = new Scanner(System.in);
        System.out.println("Hello! Welcome to the simulation.");

        // Receive ms, store in seconds.
        System.out.println("Duration for each tick (in ms) <= 1 ms: ");
        // (sec) = (ms) / (1000 ms / sec)
        tick_duration = scanner.nextDouble() / MS_PER_SEC;

        // Receive minutes, store in seconds.
        System.out.println("Simulation time (in minutes): ");
        // (min) = (min) * (60 sec / min)
        simul_duration = scanner.nextDouble() * SEC_PER_MIN;

        // (ticks) = (sec) / (sec / tick)
        num_of_ticks = (int) Math.ceil(simul_duration / tick_duration);


        System.out.println(
            "\u03BB, Average number of packets generated/arrived  (packets/sec): "
        );
        lambda = scanner.nextInt();

        System.out.println("L, Length of a packet (bits): ");
        L = scanner.nextInt();

        System.out.println("C, Transmission rate (bits/sec): ");
        C = scanner.nextInt();

        // (ticks) = ((bits) / (bits / sec)) / (sec / tick)
        t_transmission = (int) Math.ceil(
            (L / C) / tick_duration
        );

        pick_a_queue();
    }

    public static void arrival() {
        if (t >= t_arrival) {
            is_mostly_idle = false;
            KendallPacket new_packet = new KendallPacket(L);
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
            is_mostly_idle = false;
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

    public static int calc_arrival_time() {
        double u = Math.random(); // random number between 0 and 1
        double arrival_time =
            ((-1 / lambda) * Math.log(1 - u)) / tick_duration;

        return (int) Math.ceil(arrival_time);
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
            p_num_steps = 8; // 0.9, 8, 7, 6, 5, 4, 3, 2 = 8 total
            p_start =  0.2;
            p_end = 0.9;
        } else {
            is_MD1 = false;
            System.out.println("M/D/1/K queue selected.");

            System.out.println("Please enter an integer value for K, the buffer size of the queue: ");
            int K = scanner.nextInt();

            md1KQueue = new MD1KQueue(K);
            p_num_steps = 11; // 1.5, 1.4, 1.3, 1.2, 1.1, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5 = 11 total
            p_start = 0.5;
            p_end = 1.5;
            P_LOSS = new double [p_num_steps][M];
        }

        E_N = new double [p_num_steps][M];
        E_T = new int [p_num_steps][M];
        P_IDLE = new double [p_num_steps][M];

        scanner.close();
    }

    // Display outputs
    public static void create_report(int p_index) {
        for (int i = 0; i < M; i++) {
            System.out.println("E{N] for M-1= " + i + " is: " + E_N[p_index][i]);
            System.out.println("E{T] for M-1 = " + i + " is: " + E_T[p_index][i]);
            System.out.println("P_IDLE for M-1 = " + i + " is: " + P_IDLE[p_index][i]);
            if (!is_MD1) {
                System.out.println("P_LOSS for M-1 = " + i + " is: " + P_LOSS[p_index][i]);
            }
        }
    }

    public static void calculate_E_N(int M_index, int p_index) {
        int sum = 0;
        for (int i = 0; i < queue_size_list.size(); i++) {
            sum = sum + queue_size_list.get(i);
        }

        E_N[p_index][M_index] = sum/queue_size_list.size();

        // Reset variables that will be used in future intermediate calculations
        queue_size_list.clear();
    }

    public static void calculate_P_LOSS(int M_index, int p_index) {
        P_LOSS[p_index][M_index] = packets_lost/packets_generated;

        // Reset variables that will be used in future intermediate calculations
        packets_lost = 0;
        packets_generated = 0;
    }

    public static void calculate_E_T(int M_index, int p_index) {
        double average_sojourn_time = 0;

        for(int i = 0; i < sojourn_list.size(); i++) {
            average_sojourn_time = average_sojourn_time + sojourn_list.get(i)*tick_duration;
        }

        E_T[p_index][M_index] = (int)(average_sojourn_time/sojourn_list.size());

        // Reset variables that will be used in future intermediate calculations
        sojourn_list.clear();
    }

    public static void calculate_P_IDLE(int M_index, int p_index) {
        P_IDLE[p_index][M_index] = ((t_idle*tick_duration)/simul_duration); // sec/secs (ratio)

        // Reset variables that will be used in future intermediate calculations
        t_idle = 0;
    }
}