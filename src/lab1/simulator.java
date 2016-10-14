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
    static double C;
    // Duration of the simulation (sec)
    static double simul_duration;
    // Arrival time of a packet (ticks)
    static int t_arrival;
    // Departure time of a packet (ticks)
    static int t_departure;
    // Transmission time (ticks)
    static int t_transmission;
    // Duration of a single tick (sec)
//    static double tick_duration;
    // The number of times experiements are repeated
    static int M;
    // An instance of the size of the MD1 or MD1K queue
    static int queue_size;
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
    static double E_T[][]; // (ms)
    static double P_LOSS[][]; // (%)
    static double P_IDLE[][]; // (%)

    static int ticks_in_one_second;
    static long soujourn_ticks;
    static long total_packets;

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
                // Make sure that the first packet enters the queue before departing it
                t_departure = Integer.MAX_VALUE;
                // Get first packet arrival time
                t_arrival = calc_arrival_time();
                long sum_of_packets_in_queue = 0;
                soujourn_ticks = 0;
                total_packets = 0;
                for (int t = 1; t <= num_of_ticks; t++) {
                    arrival(t);
                    departure(t);

                    // more intermediate calculations for outputs
                    if (is_MD1) {
                        sum_of_packets_in_queue = sum_of_packets_in_queue + md1Queue.getSize();
                    } else {
                        sum_of_packets_in_queue = sum_of_packets_in_queue + md1KQueue.getSize();
                    }
                }

                calculate_E_N(M_index, p_index, sum_of_packets_in_queue);
                calculate_E_T(M_index, p_index);
                calculate_P_IDLE(M_index, p_index);
                if (!is_MD1) {
                    calculate_P_LOSS(M_index, p_index);
                }
            }
            create_report(p_index);
            p_index++;
            p_value += p_step_size;
        }
    }

    public static void initialize_variables() {
        M = 5;

        pick_a_queue();

        p_step_size = 0.1;

        packets_lost = 0;
        packets_generated = 0;
        t_idle = 0;


        // Ask for inputs
        scanner = new Scanner(System.in);
        System.out.println("Hello! Welcome to the simulation.");

        ticks_in_one_second =  1000000;

        // TODO could just have this be an input. That's more correct. 5million for 10,25, 50. 2.5million for inf buffer.
        num_of_ticks = 2500000;

        // TODO uncomment this
        /*
        System.out.println("L, Length of a packet (bits): ");
        L = scanner.nextInt();

        System.out.println("C, Transmission rate (bits/sec): ");
        C = scanner.nextDouble();
        */
        C = 1000000;
        L = 2000;

        lambda = p_start*C/(double)L;

        // (ticks) = ((bits) / (bits / sec)) * (ticks / sec)
        t_transmission = (int) Math.ceil(
            ((double)L / C) * ticks_in_one_second
        );

        scanner.close();
    }

    public static void arrival(int t) {
        if (t >= t_arrival) {
            total_packets++;
            is_mostly_idle = false;
            t_departure = t + t_transmission;

            KendallPacket new_packet = new KendallPacket(L);
            // TODO confirm that this is the correct tick or if it should be t_arrival
            new_packet.setT_generate(t);
            new_packet.setT_arrival(t_arrival);
            new_packet.setT_departure(t_departure);

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
        }
    }

    public static void departure(int t) {
        if (t >= t_departure) {
            is_mostly_idle = false;
            if (is_MD1) {
                KendallPacket departed_packet;
                departed_packet = md1Queue.remove();
                departed_packet.setT_finished(t);
            } else if (!is_MD1) {
                KendallPacket departed_packet;
                departed_packet = md1KQueue.remove();
                departed_packet.setT_finished(t);
            }
            soujourn_ticks = soujourn_ticks + t - t_departure + t_transmission;
            // Don't come back to this unless arrival changes the tick value
            t_departure = Integer.MAX_VALUE;
        }
    }

    public static int calc_arrival_time() {
        double u = Math.random(); // random number between 0 and 1
        double arrival_time =
            ((-1 / lambda) * Math.log(1 - u) * ticks_in_one_second);

        return (int) Math.ceil(arrival_time);
    }

    public static void pick_a_queue() {
        final String MD1 = "y";
        final String MD1K = "n";
        final String msg = "M/D/1 queue (y) or a M/D/1/K queue (n)? (y/n)";

        // TODO uncomment this
//        System.out.println(msg);
//        String choice = scanner.next();
        String choice = MD1;

        while (!(choice.equals(MD1) || choice.equals(MD1K))) {
            System.out.println(msg);
            choice = scanner.next();
        }

        if (choice.equals(MD1)) {
            is_MD1 = true;
            System.out.println("M/D/1 queue selected.");

            md1Queue = new MD1Queue();
            p_num_steps = 6; // 8, 7, 6, 5, 4, 3 = 8 total
            p_start =  0.3;
            p_end = 0.8;
        } else {
            is_MD1 = false;
            System.out.println("M/D/1/K queue selected.");

            System.out.println("Please enter an integer value for K, the buffer size of the queue: ");
            int K = scanner.nextInt();

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
        /*
        System.out.print("E_N["+p_index+"]: ");
        double running_e_n = 0;
        for (int i = 0; i < M; i++) {
            running_e_n = running_e_n + E_N[p_index][i];
        }
        running_e_n = running_e_n / M;
        System.out.println(running_e_n);
*/
        System.out.print("E_T["+p_index+"]: ");
        double running_e_t = 0;
        for (int i = 0; i < M; i++) {
            running_e_t = running_e_t + E_T[p_index][i];
        }
        running_e_t = running_e_t / M;
        System.out.println(running_e_t);
        System.out.println();

        /*
        System.out.print("P_IDLE["+p_index+"]: ");
        double running_p_idle = 0;
        for (int i = 0; i < M; i++) {
            running_p_idle = running_p_idle + P_IDLE[p_index][i];
        }
        running_p_idle = running_p_idle / M;
        System.out.println(running_p_idle);
        System.out.println();

        if(!is_MD1) {
            System.out.print("P_LOSS["+p_index+"]: ");
            double running_p_loss = 0;
            for (int i = 0; i < M; i++) {
                running_p_loss = running_p_loss + P_LOSS[p_index][i];
            }
            running_p_loss = running_p_loss / M;
            System.out.println(running_p_loss);
            System.out.println();
        }
        */
    }

    public static void calculate_E_N(int M_index, int p_index, long sum_of_packets_in_queue) {
        E_N[p_index][M_index] = ((double)sum_of_packets_in_queue/((double)num_of_ticks));
    }

    public static void calculate_P_LOSS(int M_index, int p_index) {
        P_LOSS[p_index][M_index] = packets_lost/packets_generated;

        // Reset variables that will be used in future intermediate calculations
        packets_lost = 0;
        packets_generated = 0;
    }

    public static void calculate_E_T(int M_index, int p_index) {
        E_T[p_index][M_index] = (double)soujourn_ticks/(double)total_packets;
    }

    public static void calculate_P_IDLE(int M_index, int p_index) {
        P_IDLE[p_index][M_index] = ((t_idle*ticks_in_one_second)/simul_duration); // sec/secs (ratio)

        // Reset variables that will be used in future intermediate calculations
        t_idle = 0;
    }
}