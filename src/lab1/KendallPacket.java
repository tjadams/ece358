package lab1;

public class KendallPacket {

    private int length;
    // The tick at which this packet was generated
    private int t_generate;
    // The tick at which this packet had completed its service
    private int t_finished;

    public KendallPacket(int length) {
        this.length = length;
    }

    public void setT_generate(int tick) {
        t_generate = tick;
    }

    public void setT_finished(int tick) { t_finished = tick; }

    public int getLength() {
        return length;
    }

    public int getT_finished() {
        return t_finished;
    }

    public int getT_generate() {
        return t_generate;
    }

    public int getSojournAmountOfTicks() {
        return t_finished - t_generate;
    }
}
