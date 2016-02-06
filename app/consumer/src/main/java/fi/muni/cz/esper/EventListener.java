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
import scala.App;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class EventListener implements UpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    private CuratorFramework zkSession = AppData.instance().getZkSession();

    public void update(EventBean[] newData, EventBean[] oldData) {
        logger.info("Event received.");

        if (Consumer.analyzingLevel.equals(AnalyzingLevel.LEVEL2)) { // TODO : remove this if
            logger.info("Event received with level 2. Do nothing for now. PC count: " + newData.length);
            return;
        }

        if (newData.length < 2) {
            logger.info("There is just one PC with error. This is not an attack.");
            return;
        }

        String newParent = null;
        for (int i = 0; i < newData.length; i++) {
            String source = newData[i].get("source").toString();
            if (!source.endsWith(AppData.instance().getIp())) {
                newParent = source.substring(source.lastIndexOf("/") + 1, source.length());
                break;
            }
        }
        logger.info("New Parent: " + newParent);

        for (int i = 0; i < newData.length; i++) {
            try {
                EventBean bean = newData[i];
                String source = bean.get("source").toString();

                logger.info("Event data. Source: " + bean.get("source") + ", count: " + bean.get("cnt"));

                JSONObject data = new JSONObject();
                data.put("action", ActionType.CREATE.toString());
                data.put("appMode", "consumer");
                data.put("level", "LEVEL2");

                zkSession.setData().forPath(AppData.ZK_ROOT + "/" + newParent, data.toString().getBytes());

                data.put("action", ActionType.MOVE.toString());
                data.put("appMode", "producer");
                data.put("parent", newParent);
                data.put("path", AppData.ZK_ROOT + "//" + newParent + source.substring(source.lastIndexOf("/"), source.length()));

                zkSession.setData().forPath(source, data.toString().getBytes());
            } catch (Exception e) {
                logger.error("Sending data failed in Esper event handler.", e);
                return;
            }
        }

    }
}
