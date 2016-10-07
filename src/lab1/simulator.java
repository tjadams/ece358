package lab1;

import java.util.Scanner;

// Note that this class is a singleton so we'll use global variables
public class simulator {
    // TODO should we care about floating point values?
    static double t_arrival;
    static int lambda;
    static int L;
    static int C;
    static int num_of_ticks;
    static Scanner scanner;
    static boolean is_m_d_one;
    static KendallQueue queue; // TODO create this class, a superclass of MD1 and MD1K queues

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

    public static double calc_arrival_time() {
        double u = Math.random(); // random number between 0 and 1
        return ((-1 / lambda) * Math.log(1 - u)); // exponential random variable
    }

    public static void pick_a_queue() {
        System.out.println("Would you like to simulate using a M/D/1 queue (y) or a M/D/1/K queue (n)? (y/n)");
        String m_d_one_choice = scanner.next();
        while (!(m_d_one_choice.equals("y") || m_d_one_choice.equals("n"))) {
            System.out.println("Please enter 'y' for M/D/1 or 'n' for M/D/1/K: ");
            m_d_one_choice = scanner.next();
        }

        if (m_d_one_choice.equals("y")) {
            is_m_d_one = true;
            scanner.close();
            System.out.println("M/D/1 queue selected.");
            // TODO set queue equal to a new md1 queue
        } else if (m_d_one_choice.equals("n")) {
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
}