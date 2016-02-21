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

            // --------------------- CHECK LIVING NODES -----------------------------------

            List<EventBean> livingNodes = new ArrayList<>();

            for (int i = 0; i < newData.length; i++) {
                String source = newData[i].get("source").toString();

                if (zkSession.checkExists().forPath(source) == null) {
                    logger.warn("Node already exists. Path: " + source);
                    continue;
                }
                livingNodes.add(newData[i]);
            }

            if (livingNodes.size() <= 1) {
                logger.warn("There left only one or zero nodes. Nothing to do. " + livingNodes.toString());
                return;
            }

            // --------------------- NEW PARENT -----------------------------------
            String newParent = null;
            for (EventBean bean : livingNodes) {
                String source = bean.get("source").toString();
                if (!source.endsWith(AppData.instance().getIp())) {
                    newParent = source.substring(source.lastIndexOf("/") + 1, source.length());
                    break;
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

            // --------------------- NEW PRODUCERS -----------------------------------

            for (EventBean bean : livingNodes) {
                String source = bean.get("source").toString();

                logger.info("Event data. Source: " + bean.get("source") + ", count: " + bean.get("cnt"));

                data.put("action", ActionType.CREATE_CHILDREN.toString());
                data.put("appMode", "producer");
                data.put("parent", newParent);
                data.put("path", AppData.ZK_ROOT + "/" + newParent + source.substring(source.lastIndexOf("/"), source.length()));

                zkSession.setData().forPath(AppData.ZK_ROOT + "/" + newParent, data.toString().getBytes());
            }

            logger.info("Childrens: " + zkSession.getChildren().forPath(AppData.ZK_ROOT + "/" + newParent).toString());

        } catch (Exception e) {
            logger.error("Sending data failed in Esper event handler.", e);
            return;
        }
    }

}
