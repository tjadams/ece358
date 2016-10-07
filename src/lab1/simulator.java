package lab1;

import java.util.Scanner;

// Note that this class is a singleton so we'll use global variables
public class simulator {
    static int C;                           // transmission rate in bits per second
    static boolean is_m_d_one;
    static int lambda;                      // packets per second
    static int L;                           // length of packet in bits
    static int num_of_ticks;
    // TODO should we care about floating point values?
    static long t;                          // elapsed time of simulation aka t in the pseudocode (ms)
    static long t_arrival;                  // the arrival time of a packet (ms)
    static long t_departure;                // the departure time of a packet (ms)
    static long t_start;                    // start time of simulation, reference point in t (ms)

    static final int ms_per_second = 1000;  // how many milliseconds are in one second

    // TODO create this class, a superclass of MD1 and MD1K queues
    static KendallQueue queue;
    static Scanner scanner;

    public static void main(String args[]) {
        initialize_variables();
        t_arrival = calc_arrival_time(); // calculate first packet arrival time
        // TODO appendix B says to repeat this for loop like 5 times or something...
        for (int i = 0; i <= num_of_ticks; i++) {
            arrival();
            departure();
        }
        create_report();
    }

    public static void arrival() {
        t = now() - t_start;
        if (t >= t_arrival) {
            KendallPacket packet = new KendallPacket(L);
            queue.add(packet);
            t_arrival = t + calc_arrival_time();
            t_departure = t + (L / C) * ms_per_second;  // bits/(bits/second)*1000(ms/second) = ms
            // TODO add support for the packet loss case of MD1K where you can't add to the queue. Maybe do something like have queue.add() return a boolean representing success. If it's false then increase number of packets lost by 1. This would only be false in the situation that you have a MD1K queue and that queue is full and you try to add a packet to it
        }
    }

    public static void departure() {
        t = now() - t_start;
        if (t >= t_departure) {
            queue.pop();
        }
    }

    public static long calc_arrival_time() {
        double u = Math.random(); // random number between 0 and 1
        double arrival_time_in_seconds = ((-1 / lambda) * Math.log(1 - u)); // exponential random variable
        double arrival_time_in_milliseconds = arrival_time_in_seconds * ms_per_second;
        return (long) arrival_time_in_milliseconds;
    }

    public static void pick_a_queue() {
        System.out.println("Would you like to simulate using a M/D/1 queue (y) or a M/D/1/K queue (n)? (y/n)");
        String m_d_one_choice = scanner.next();
        final String YES = "y";
        final String NO = "n";
        while (!(m_d_one_choice.equals(YES) || m_d_one_choice.equals(NO))) {
            System.out.println("Please enter 'y' for M/D/1 or 'n' for M/D/1/K: ");
            m_d_one_choice = scanner.next();
        }

        if (m_d_one_choice.equals(YES)) {
            is_m_d_one = true;
            scanner.close();
            System.out.println("M/D/1 queue selected.");
            // TODO set queue equal to a new md1 queue
        } else if (m_d_one_choice.equals(NO)) {
            is_m_d_one = false;
            System.out.println("M/D/1/K queue selected.");

            System.out.println("Please enter an integer value for K, the buffer size of the queue: ");
            int K = scanner.nextInt();

            scanner.close();
            // TODO set queue equal to a new md1k queue
        }
    }

    public static void initialize_variables() {
        // Ask for inputs
        scanner = new Scanner(System.in);
        System.out.println("Hello! Welcome to the simulation.");

        System.out.println("Please enter an integer value for lambda: ");
        lambda = scanner.nextInt(); // average number of packets generated/arrived in units of packets per second

        System.out.println("Please enter an integer value for L: ");
        L = scanner.nextInt(); // # of bits in a packet

        System.out.println("Please enter an integer value for C: ");
        C = scanner.nextInt(); // service time of each packet in bits per second

        System.out.println("Please enter an integer value for the number of ticks: ");
        num_of_ticks = scanner.nextInt();

        // int p = L*lambda/C; // utilization of the queue (input rate/service rate)

        pick_a_queue();

        t_start = now();
    }

    public static void create_report() {
        if (is_m_d_one) {
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

    public static long now() {
        // TODO: Once we finish, let's see if `System.nanoTime()` makes any significant difference.
        return System.currentTimeMillis();
    }
}