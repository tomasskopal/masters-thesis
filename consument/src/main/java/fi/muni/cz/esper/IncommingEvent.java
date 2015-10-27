package fi.muni.cz.esper;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class IncommingEvent {

    private String msg;
    private String identifier;
    private String severity;

    public IncommingEvent(String msg) {
        this.msg = msg;
        parseMsg(msg);
    }

    private void parseMsg(String msg) { // TODO: better parsing please. Use regex
        if (msg.contains("Level1")) {
            this.severity = "Level1";
        } else if (msg.contains("Level2")) {
            this.severity = "Level2";
        }

        if (msg.contains("PC1")) {
            this.identifier = "PC1";
        } else if (msg.contains("PC2")) {
            this.identifier = "PC2";
        }
    }

    @Override
    public String toString() {
        return "IncommingEvent{" +
                "msg='" + msg + '\'' +
                ", identifier='" + identifier + '\'' +
                ", severity=" + severity +
                '}';
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}