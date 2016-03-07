package fi.muni.cz.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import fi.muni.cz.ActionType;
import fi.muni.cz.AppData;
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

    private String epRule;

    public EventListener(String epRule) {
        this.epRule = epRule;
    }

    public void update(EventBean[] newData, EventBean[] oldData) {
        try {
            logger.info("Event received. " + this.epRule);

            if (newData.length < 2) {
                logger.info("There is just one PC with error. This is not an attack.");
                return;
            }

            if (newData[0].get("level").toString().equals("LEVEL2")) {
                logger.info("Event received with level 2. Printing info. PC count: " + newData.length);
                for (EventBean bean : newData) {
                    logger.info("Event data. Source: " + bean.get("source") + ", count: " + bean.get("cnt"));
                }
                return;
            }

            // --------------------- NEW PARENT -----------------------------------
            String newParent = null;
            for (EventBean bean : newData) {
                String source = bean.get("source").toString();
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
            data.put("ttl", "10000");
            zkSession.setData().forPath(AppData.ZK_ROOT + "/" + newParent, data.toString().getBytes());
            Thread.sleep(1000);
            logger.info("Create new consumer at: " + AppData.ZK_ROOT + "/" + newParent + ". Data: " + data.toString());

            // --------------------- NEW PRODUCERS -----------------------------------

            for (EventBean bean : newData) {
                String source = bean.get("source").toString();
                String source_ip = source.substring(source.lastIndexOf("/") + 1, source.length());

                data = new JSONObject();
                data.put("action", ActionType.CREATE_CHILDREN_PRODUCER.toString());
                data.put("appMode", "producer");
                data.put("parent", newParent);
                data.put("level", "LEVEL2");
                data.put("path", AppData.ZK_ROOT + "/" + newParent + "/" + source_ip);

                zkSession.setData().forPath(AppData.ZK_ROOT + "/" + source_ip, data.toString().getBytes());
                logger.info("Set data at node: " + AppData.ZK_ROOT + "/" + source_ip + ". Data: " + data.toString());
                Thread.sleep(1000);
            }

            logger.info("EpRule: " + this.epRule);
            for (EventBean bean : newData) {
                logger.info("Bean: " + bean.get("source").toString());
            }


            if (newData[0].get("level").toString().equals("LEVEL1")) { // TODO : this if is just for the testing
                data = new JSONObject();
                data.put("action", ActionType.INACTIVE_CONSUMER.toString());
                zkSession.setData().forPath(AppData.ZK_ROOT + "/147.251.43.181", data.toString().getBytes());
            }

        } catch (Exception e) {
            logger.error("Sending data failed in Esper event handler.", e);
            return;
        }
    }

}
