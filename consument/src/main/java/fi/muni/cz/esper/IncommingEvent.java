package fi.muni.cz.esper;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class IncommingEvent {
    private String msg;

    public IncommingEvent(String msg) {
        this.msg = msg;
    }

    public String toString() {
        return "Msg:" + msg;
    }

}