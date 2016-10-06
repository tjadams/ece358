package lab1;

import java.util.Scanner;

public class simulator {
    public static void main (String args[]) {
        // Ask for inputs
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hello! Welcome to the simulation.");

        System.out.println("Please enter an integer value for lambda: ");
        int lambda = scanner.nextInt(); // average number of packets generated/arrived in units of packets per second

        System.out.println("Please enter an integer value for L: ");
        int L = scanner.nextInt(); // # of bits in a packet

        System.out.println("Please enter an integer value for C: ");
        int C = scanner.nextInt(); // service time of each packet in bits per second

        // int p = L*lambda/C; // utilization of the queue (input rate/service rate)

        System.out.println("Would you like to simulate using a M/D/1 queue (y) or a M/D/1/K queue (n)? (y/n)");
        String MDOneChoice = scanner.next();
        while (!(MDOneChoice.equals("y") || MDOneChoice.equals("n"))) {
            System.out.println("Please enter 'y' for M/D/1 or 'n' for M/D/1/K: ");
            MDOneChoice = scanner.next();
        }

        boolean isMDOne = false;
        if (MDOneChoice.equals("y")) {
            isMDOne = true;
        } else if (MDOneChoice.equals("n")) {
            isMDOne = false;
        }

        // Simulate
        if (isMDOne) {
            scanner.close();
            System.out.println("M/D/1 queue selected.");

            // Simulate MDOne and display outputs of MDOne
            simulateMDOne(lambda, L, C);
        } else {
            System.out.println("M/D/1/K queue selected.");

            System.out.println("Please enter an integer value for K, the buffer size of the queue: ");
            int K = scanner.nextInt();

            scanner.close();

            // Simulate MDOneK and display outputs of MDOneK
            simulateMDOneK(lambda, L, C, K);
        }
    }

    public static void simulateMDOne(int lambda, int L, int C) {
        // TODO ...
        MDOneQueue my_MDOneQueue = new MDOneQueue(lambda, L, C);
        float interPacketArrivalTimeX; // TODO create this exponential random variable and follow the "providedLab1Pseudocode.txt" file

        // Display outputs of MDOne
        int E_N;
        int E_T;
        int P_IDLE;
    }

    public static void simulateMDOneK(int lambda, int L, int C, int K) {
        // TODO ...

        // Display outputs of MDOneK
        int E_N;
        int E_T;
        int P_IDLE;
        int P_LOSS;
    }
}