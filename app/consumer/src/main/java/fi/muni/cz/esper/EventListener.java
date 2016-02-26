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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class EventListener implements UpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    private CuratorFramework zkSession = AppData.instance().getZkSession();

    public void update(EventBean[] newData, EventBean[] oldData) {
        try {
            logger.info("Event received.");

            if (Consumer.analyzingLevel.equals(AnalyzingLevel.LEVEL2)) { // TODO : remove this if
                logger.info("Event received with level 2. Do nothing for now. PC count: " + newData.length);
                return;
            }


            if (newData.length < 2) {
                logger.info("There is just one PC with error. This is not an attack.");
                return;
            }

            // --------------------- NEW PARENT -----------------------------------
            String newParent = null;
            for (int i = 0; i < newData.length; i++) {
                String source = newData[i].get("source").toString();
                String newParentCandidate = source.substring(source.lastIndexOf("/") + 1, source.length());
                if (zkSession.getChildren().forPath(AppData.ZK_ROOT + "/" + newParentCandidate).size() == 0) {
                    newParent = newParentCandidate;
                }
            }

            logger.info("New Parent: " + newParent);

            if (newParent == null) {
                logger.warn("Finding new parent failed for data: " + newData);
                return;
            }

            // --------------------- NEW CONSUMENT -----------------------------------

            // create new consumer
            JSONObject data = new JSONObject();
            data.put("action", ActionType.CREATE.toString());
            data.put("appMode", "consumer");
            data.put("level", "LEVEL2");
            data.put("path", AppData.ZK_ROOT + "/" + newParent);
            zkSession.setData().forPath(AppData.ZK_ROOT + "/" + newParent, data.toString().getBytes());
            Thread.sleep(1000);
            logger.info("Create new consumer at: " + AppData.ZK_ROOT + "/" + newParent + ". Data: " + data.toString());

            // --------------------- NEW PRODUCERS -----------------------------------

            for (int i = 0; i < newData.length; i++) {
                String source = newData[i].get("source").toString();
                String source_ip = source.substring(source.lastIndexOf("/") + 1, source.length());

                data = new JSONObject();
                data.put("action", ActionType.CREATE_CHILDREN.toString());
                data.put("appMode", "producer");
                data.put("parent", newParent);
                data.put("level", "LEVEL2");
                data.put("path", AppData.ZK_ROOT + "/" + newParent + "/" + source_ip);

                zkSession.setData().forPath(AppData.ZK_ROOT + "/" + source_ip, data.toString().getBytes());
                logger.info("Set data at node: " + AppData.ZK_ROOT + "/" + source_ip + ". Data: " + data.toString());
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            logger.error("Sending data failed in Esper event handler.", e);
            return;
        }
    }

}
