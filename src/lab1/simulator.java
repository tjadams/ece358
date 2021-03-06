package lab1;

import java.util.Scanner;

// Note that this class is a singleton so we'll use global variables
public class simulator {
    // Parameters from the lab manual
    static double lambda;
    static int L;
    static double C;
    // True if the queue is a MD1Queue, false if MD1KQueue
    static boolean is_MD1;
    // Total number of ticks for the simulation (ticks)
    static int num_of_ticks;
    // Arrival time of a packet (ticks)
    static int t_arrival;
    // Departure time of a packet (ticks)
    static int t_departure;
    // Transmission time (ticks)
    static int t_transmission;
    // The number of times experiments are repeated
    static int M;
    // An instance of the size of the MD1 or MD1K queue
    static int queue_size;

    // Represents the rho inequalities as in question 2 and 4 from the report
    static double p_step_size;
    static double p_start;
    static double p_end;
    // Number of iterations with p_step_size
    static int p_num_steps;

    // Outputs (array size is p_num_steps*M)
    static double E_N[][]; // avg # of packets in the queue (# packets)
    static double E_T[][]; // (ms)
    static double P_LOSS[][]; // (%)
    static double P_IDLE[][]; // (%)

    static int ticks_in_one_second;
    static long soujourn_ticks;
    static long total_packets;
    static int ticks_idle;
    static int packets_dropped;
    static long sum_of_packets_in_queue;

    static MD1Queue md1Queue;
    static MD1KQueue md1KQueue;
    static Scanner scanner;

    public static void main(String args[]) {
        initialize_variables();

        int p_index = 0;
        double p_value = p_start;
        while (p_index < p_num_steps) {
            lambda = (p_value*C/((double)L));
            for (int M_index = 0; M_index < M; M_index++) {
                // Get first packet arrival time and departure time
                t_arrival = calc_arrival_time();
                t_departure = t_arrival + t_transmission;

                // Clear variables used in intermediate calculations
                sum_of_packets_in_queue = 0;
                soujourn_ticks = 0;
                total_packets = 0;
                ticks_idle = 0;
                packets_dropped = 0;
                for (int t = 1; t <= num_of_ticks; t++) {
                    arrival(t);
                    departure(t);

                    // Intermediate calculations for outputs
                    if (is_MD1) {
                        sum_of_packets_in_queue = sum_of_packets_in_queue + md1Queue.getSize();
                        if (md1Queue.getSize() == 0) {
                            ticks_idle++;
                        }
                    } else {
                        sum_of_packets_in_queue = sum_of_packets_in_queue + md1KQueue.getSize();
                        if (md1KQueue.getSize() == 0) {
                            ticks_idle++;
                        }
                    }
                }
                calculate_E_N(M_index, p_index);
                calculate_E_T(M_index, p_index);
                calculate_P_IDLE(M_index, p_index);
                if (!is_MD1) {
                    calculate_P_LOSS(M_index, p_index);
                }
            }
            System.out.println("Printing outputs for Rho = " + p_value);
            create_report(p_index);
            p_index++;
            p_value += p_step_size;
        }
    }

    public static void initialize_variables() {
        M = 5;

        // Prepare to ask for inputs
        scanner = new Scanner(System.in);
        System.out.println("Hello! Welcome to the simulation. Rho/Lambda are set to their corresponding values in Q2 and Q4 of the report.");

        pick_a_queue();

        p_step_size = 0.1;

        ticks_in_one_second =  1000;

        System.out.println("Enter the number of ticks: ");
        num_of_ticks = scanner.nextInt();
//        num_of_ticks = 2500000;

        System.out.println("Enter L, Length of a packet (bits): ");
        L = scanner.nextInt();
//        L = 2000;

        System.out.println("Enter C, Transmission rate (bits/sec): ");
        C = scanner.nextDouble();
//        C = 1000000;
        System.out.println();

        lambda = p_start*C/(double)L;

        // (ticks) = ((bits) / (bits / sec)) * (ticks / sec)
        t_transmission = (int) Math.ceil(
            ((double)L /(double)C) * (double)ticks_in_one_second
        );

        scanner.close();
    }

    public static void arrival(int t) {
        if (t >= t_arrival) {
            total_packets++;
            t_departure = t + t_transmission;

            KendallPacket new_packet = new KendallPacket(L);

            if (is_MD1) {
                md1Queue.add(new_packet);
                queue_size = md1Queue.getSize();
            } else {
                if (md1KQueue.getSize() == md1KQueue.getK()) {
                    packets_dropped++;
                } else {
                    md1KQueue.add(new_packet);
                    queue_size = md1KQueue.getSize();
                }
            }

            t_arrival = t + calc_arrival_time();
        }
    }

    public static void departure(int t) {
        if (t >= t_departure) {
            if (is_MD1 && md1Queue.getSize() != 0) {
                md1Queue.remove();
                soujourn_ticks = soujourn_ticks + t - t_departure + t_transmission;
            } else if (!is_MD1 && md1KQueue.getSize() != 0) {
                md1KQueue.remove();
                soujourn_ticks = soujourn_ticks + t - t_departure + t_transmission;
            }
        }
    }

    public static int calc_arrival_time() {
        double u = Math.random(); // random number between 0 and 1
        double arrival_time =
            (((double)-1 / (double)lambda) * (double)Math.log(1 - u) * (double)ticks_in_one_second);

        return (int) Math.ceil(arrival_time);
    }

    public static void pick_a_queue() {
        final String MD1 = "y";
        final String MD1K = "n";
        final String msg = "M/D/1 queue (y) or a M/D/1/K queue (n)? (y/n)";

        System.out.println(msg);
        String choice = scanner.next();
//        String choice = MD1K;

        while (!(choice.equals(MD1) || choice.equals(MD1K))) {
            System.out.println(msg);
            choice = scanner.next();
        }

        if (choice.equals(MD1)) {
            is_MD1 = true;
            System.out.println("M/D/1 queue selected.");

            md1Queue = new MD1Queue();
            p_num_steps = 6; // 0.8, 7, 6, 5, 4, 3 = 8 total
            p_start =  0.3;
            p_end = 0.8;
        } else {
            is_MD1 = false;
            System.out.println("M/D/1/K queue selected.");

            System.out.println("Please enter an integer value for K, the buffer size of the queue: ");
            int K = scanner.nextInt();
//            int K = 50;

            md1KQueue = new MD1KQueue(K);
            p_num_steps = 9; // 1.4, 1.3, 1.2, 1.1, 1.0, 0.9, 0.8, 0.7, 0.6 = 9 total
            p_start = 0.6;
            p_end = 1.4;
            P_LOSS = new double [p_num_steps][M];
        }

        E_N = new double [p_num_steps][M];
        E_T = new double [p_num_steps][M];
        P_IDLE = new double [p_num_steps][M];
    }

    // Display outputs
    public static void create_report(int p_index) {
        System.out.print("E_N["+p_index+"]: ");
        double running_e_n = 0;
        for (int i = 0; i < M; i++) {
            running_e_n = running_e_n + E_N[p_index][i];
        }
        running_e_n = running_e_n / M;
        System.out.println(running_e_n);

        System.out.print("E_T["+p_index+"]: ");
        double running_e_t = 0;
        for (int i = 0; i < M; i++) {
            running_e_t = running_e_t + E_T[p_index][i];
        }
        running_e_t = running_e_t / M;
        System.out.println(running_e_t);

        System.out.print("P_IDLE["+p_index+"]: ");
        double running_p_idle = 0;
        for (int i = 0; i < M; i++) {
            running_p_idle = running_p_idle + P_IDLE[p_index][i];
        }
        running_p_idle = running_p_idle / M;
        System.out.println(running_p_idle);

        if(!is_MD1) {
            System.out.print("P_LOSS["+p_index+"]: ");
            double running_p_loss = 0;
            for (int i = 0; i < M; i++) {
                running_p_loss = running_p_loss + P_LOSS[p_index][i];
            }
            running_p_loss = running_p_loss / M;
            System.out.println(running_p_loss);
        }

        System.out.println();
    }

    public static void calculate_E_N(int M_index, int p_index) {
        E_N[p_index][M_index] = (double)sum_of_packets_in_queue/(double)num_of_ticks;
    }

    public static void calculate_P_LOSS(int M_index, int p_index) {
        P_LOSS[p_index][M_index] = packets_dropped/(double)total_packets;
    }

    public static void calculate_E_T(int M_index, int p_index) {
        E_T[p_index][M_index] = (double)soujourn_ticks*ticks_in_one_second/(double)total_packets;
    }

    public static void calculate_P_IDLE(int M_index, int p_index) {
        P_IDLE[p_index][M_index] = (double)ticks_idle/(double)num_of_ticks;
    }
}