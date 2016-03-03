package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tomasskopal on 19.12.15.
 */
public class DataChangeListener implements NodeCacheListener {

    private static Logger logger;

    private NodeCache dataCache;
    private static final Map<String, DataProducer> dataProducers = new HashMap<>();
    private static List<Consumer> dataConsumers = new ArrayList<>();

    public DataChangeListener(NodeCache cache) {
        logger = AppData.instance().getLogger();
        this.dataCache = cache;
    }

    @Override
    public void nodeChanged() throws Exception {
        if (dataCache.getCurrentData() == null) {
            logger.info("Node was deleted");
            return;
        }
        String data = new String(dataCache.getCurrentData().getData(), StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();

        try{
            JSONObject json = (JSONObject) parser.parse(data);
            logger.info("Incoming parsed data: " + json.toJSONString());

            evaluateData(json);
        }
        catch(ParseException pe){
            logger.error("Unable to parse data. Position: " + pe.getPosition() + ". Data: " + data);
            return;
        }
        logger.info("------------------------------");
    }

    private void evaluateData(JSONObject json) throws Exception {
        logger.info("Evaluated action will be: " + json.get("action"));
        CuratorFramework curatorFramework = AppData.instance().getZkSession();

        switch (ActionType.valueOf((String) json.get("action"))) {
            case CREATE:
                if (json.get("appMode").equals("producer")) {
                    createProducer((String) json.get("level"), (String) json.get("parent"), (String) json.get("path"));
                }
                if (json.get("appMode").equals("consumer")) {
                    createConsumer(
                            AnalyzingLevel.valueOf((String) json.get("level")),
                            json.get("ttl"),
                            (String) json.get("path"),
                            null
                    );
                    MainApp.registerChildrenWatcher((String) json.get("path"));
                }
                break;
            case CREATE_CHILDREN:
                JSONObject data = new JSONObject();
                data.put("action", ActionType.CREATE.toString());
                data.put("appMode", "producer");
                data.put("isBasic", String.valueOf(false));
                data.put("path", json.get("path"));
                data.put("parent", json.get("parent"));
                data.put("level", json.get("level"));

                MainApp.createNodeAndRegisterWatcher((String) json.get("path"));
                Thread.sleep(1000);
                curatorFramework.setData().forPath((String) json.get("path"), data.toString().getBytes());
                break;
            case STOP_PRODUCER:
                String path = (String) json.get("path");
                logger.info(dataProducers.keySet());
                dataProducers.get(path).stop();
                dataProducers.remove(path);
                curatorFramework.delete().guaranteed().forPath(path);
                break;
            case STOP_CONSUMER:
                logger.info("Try to stop consumer. At PC (ip): " + AppData.instance().getIp());
                dataConsumers.get(0).stop();
                dataConsumers.clear();
                break;
            case INACTIVE_CONSUMER:
                dataConsumers.get(0).inactive();
                break;
            case SET_EP_RULE:
                logger.info("Try to replace consumer. Old: " + dataConsumers + " at PC (ip): " + AppData.instance().getIp());
                dataConsumers.get(0).stop();
                while (!dataConsumers.get(0).isTerminated()) {
                    logger.info("Data consumer is still not terminated");
                    Thread.sleep(500);
                }
                dataConsumers.clear();
                createConsumer(
                        AnalyzingLevel.LEVEL1,
                        null,
                        null,
                        EpRules.instance().getRule((String) json.get("rule"))
                );
                break;
        }
    }

    private void createConsumer(AnalyzingLevel analyzingLevel, Object ttl, final String path, String rule) {
        String epRule = rule;
        if (epRule == null) {
            switch (analyzingLevel) {
                case LEVEL1:
                    epRule = EpRules.instance().getRule("PORT_SCAN");
                    break;
                case LEVEL2:
                    epRule = EpRules.instance().getRule("LEVEL2");
                    break;
            }
        }
        Consumer consumer = new Consumer(AppData.instance().getIp(), epRule);
        consumer.run(1);
        dataConsumers.add(consumer);
        logger.info("------------- Consumer was created from incoming z-node data ------------- ");

        if (ttl != null) {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() { // TODO create new class GroupTimeToLive
                        @Override
                        public void run() {
                            CuratorFramework curatorFramework = AppData.instance().getZkSession();
                            try {
                                for (String children : curatorFramework.getChildren().forPath(path)) {
                                    String children_path = path + "/" + children;
                                    logger.info("Time is up. Stopping producer: " + children_path);

                                    JSONObject data = new JSONObject();
                                    data.put("action", ActionType.STOP_PRODUCER.toString());
                                    data.put("path", children_path);
                                    curatorFramework.setData().forPath(AppData.ZK_ROOT + "/" + children, data.toString().getBytes());
                                    logger.info("Event about stopping producer was send to the node: " + AppData.ZK_ROOT + "/" + children);
                                    Thread.sleep(1000);
                                }
                            } catch (Exception e) {
                                logger.error("Something in the tim failed", e);
                            }
                        }
                    },
                    Integer.valueOf((String) ttl)
            );
        }
    }

    private void createProducer(String level, String parent, String path) {
        DataProducer dataProducer = new DataProducer(level, parent, path);
        Thread producer = new Thread(
            dataProducer
        );
        producer.start();
        dataProducers.put(path, dataProducer);
        logger.info(dataProducers.keySet());
        logger.info("------------- Producer was created from incoming z-node data. With path: " + path + "-------------------- ");
    }
}
