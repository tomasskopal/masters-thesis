package fi.muni.cz.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class EventListener implements UpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    public void update(EventBean[] newData, EventBean[] oldData) {
        logger.info(String.valueOf(newData.length));
        for (EventBean bean : newData) {
            logger.info("Event data. Source: " + bean.get("source") + ", count: " + bean.get("cnt"));
        }
    }
}
