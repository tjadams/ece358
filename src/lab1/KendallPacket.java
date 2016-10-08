package lab1;

public class KendallPacket {

    private int length;
    private long t_generate;

    public KendallPacket(int length) {
        this.length = length;
    }

    public void setTimeGenerated(long time) {
        t_generate = time;
    }
}
