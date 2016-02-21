package fi.muni.cz.esper;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class IncommingEvent {

    private static final Logger logger = LoggerFactory.getLogger(IncommingEvent.class);

    private String msg;
    private String source;
    private String level;

    private String flag;
    private String port;
    private String size;

    public IncommingEvent(String data) {
        JSONParser parser = new JSONParser();

        try{
            JSONObject json = (JSONObject) parser.parse(data);
            this.msg = (String)json.get("msg");
            this.source = (String)json.get("source");
            this.level = (String)json.get("level");
            this.flag = (String)json.get("flag");
            this.port = (String)json.get("port");
            this.size = (String)json.get("size");
        }
        catch(ParseException pe){
            logger.error("Unable to parse data. Position: " + pe.getPosition() + ". Data: " + data);
            return;
        }
    }

    @Override
    public String toString() {
        return "IncommingEvent{" + source + ", " + level + "}";
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}