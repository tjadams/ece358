public class Simulator {
    public static void main (String args[]) {
        // Ask for inputs
        boolean isMDOne = false;
        float lambda; // average number of packets generated/arrived in units of packets per second
        int L; // # of bits in a packet
        float C; // service time of each packet in bits per second
        float p = L*lambda/C; // utilization of the queue (input rate/service rate)

        // Simulate
        if (isMDOne) {
            // Simulate MDOne

            // Display outputs of MDOne
            int E_N;
            int E_T;
            int P_IDLE;
        } else {
            // Ask for K
            int K;

            // Simulate MDOneK

            // Display outputs of MDOneK
            int E_N;
            int E_T;
            int P_IDLE;
            int P_LOSS;
        }
    }
}