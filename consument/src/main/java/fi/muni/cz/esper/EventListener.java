package fi.muni.cz.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class EventListener implements UpdateListener {

    public void update(EventBean[] newData, EventBean[] oldData) {
        System.out.println("Event received: " + newData[0].getUnderlying());
    }
}
