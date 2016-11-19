package lab2;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimulatorTest {
    @Before
    public void setup() {
        // Clear the Simulator's changing variables
        Simulator.M = 0;
        Simulator.node = new Node();

        // Clear the Simulator's constants
        // TODO make sure the constants you want are set in your tests otherwise they'll be the default values
    }

    @Test(expected=NullPointerException.class)
    public void testSenseMediumInvalidNode() {
        Simulator.senseMedium(null, 2);
    }

    @Test()
    public void testSenseMediumInvalidTick() {
        Simulator.senseMedium(Simulator.node, 0);
        System.out.println("Expect to see an invalid input error above");
    }

//    @Test()
//    public void testBitTimeToTicks() {
//    }

    @Test()
    public void testSenseMedium() {
        // 1 tick + 96 bit time in ticks where W is 10Mbps =
        // 1 tick + 960 ticks where 960 = 96*(1/(10*1000000))/(10*(1/1000000000))
        // = 961

        // Setup state_end_tick
        Simulator.senseMedium(Simulator.node, 1);
        assertEquals(961, Simulator.node.state_end_tick);
    }
}