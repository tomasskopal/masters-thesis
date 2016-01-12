package fi.muni.cz.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import fi.muni.cz.ActionType;
import fi.muni.cz.AnalyzingLevel;
import fi.muni.cz.AppData;
import fi.muni.cz.Consumer;
import org.apache.curator.framework.CuratorFramework;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class EventListener implements UpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    private CuratorFramework zkSession = AppData.instance().getZkSession();

    public void update(EventBean[] newData, EventBean[] oldData) {
        logger.info("Event received.");
        
        if (Consumer.analyzingLevel.equals(AnalyzingLevel.LEVEL2)) { // TODO : remove this if
            logger.info("Event received with level 2. Do nothing for now");
            return;
        }

        if (newData.length < 2) {
            logger.info("There is just one PC with error. This is not an attack.");
            return;
        }
        for (int i = 0; i < newData.length; i++) {
            EventBean bean = newData[i];
            logger.info("Event data. Source: " + bean.get("source") + ", count: " + bean.get("cnt"));

            JSONObject data = new JSONObject();
            data.put("action", ActionType.MOVE.toString());
            data.put("parent", newData[0].get("source").toString());
            if (i == 0) {
                data.put("appMode", "combined");
            } else {
                data.put("appMode", "producer");
            }

            try {
                zkSession.setData().forPath(AppData.ZK_ROOT + "/" + bean.get("source").toString(), data.toString().getBytes());
            } catch (Exception e) {
                logger.error("Sending data failed in Esper event handler.", e);
                return;
            }
        }
    }
}
