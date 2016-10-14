package lab1;

public class KendallPacket {

    private int length;
    // The tick at which this packet was generated
    private int t_generate;
    // The tick at which this packet had completed its service
    private int t_finished;
    private int t_arrival;
    private int t_departure;

    public KendallPacket(int length) {
        this.length = length;
    }

    public void setT_generate(int tick) {
        t_generate = tick;
    }

    public void setT_finished(int tick) { t_finished = tick; }

    public void setT_arrival(int t_arrival) {
        this.t_arrival = t_arrival;
    }

    public void setT_departure(int t_departure) {
        this.t_departure = t_departure;
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

    public int getT_arrival() {
        return t_arrival;
    }

    public int getT_departure() {
        return t_departure;
    }
}
