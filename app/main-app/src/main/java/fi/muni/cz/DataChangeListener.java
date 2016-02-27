package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tomasskopal on 19.12.15.
 */
public class DataChangeListener implements NodeCacheListener {

    private static Logger logger;

    private NodeCache dataCache;
    private Map<String, DataProducer> dataProducers = new HashMap<>();
    private Consumer dataConsumer = null;

    public DataChangeListener(NodeCache dataCache) {
        logger = AppData.instance().getLogger();
        this.dataCache = dataCache;
    }

    @Override
    public void nodeChanged() throws Exception {
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
        switch (ActionType.valueOf((String) json.get("action"))) {
            case CREATE:
                if (json.get("appMode").equals("producer")) {
                    createProducer((String) json.get("level"), (String) json.get("parent"), (String) json.get("path"));
                }
                if (json.get("appMode").equals("consumer")) {
                    createConsumer(
                            AnalyzingLevel.valueOf((String) json.get("level")),
                            json.get("ttl"),
                            (String) json.get("path")
                    );
                    MainApp.registerChildrenWatcher((String) json.get("path"));
                }
                break;
            case CREATE_CHILDREN:
                CuratorFramework curatorFramework = AppData.instance().getZkSession();
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
        }
    }

    private void createConsumer(AnalyzingLevel analyzingLevel, Object ttl, final String path) {
        String epRule = null;
        switch (analyzingLevel) {
            case LEVEL1:
                epRule = EpRules.instance().getRule("SYN_FLOOD");
                break;
            case LEVEL2:
                epRule = EpRules.instance().getRule("LEVEL2");
                break;
        }
        Consumer consumer = new Consumer(AppData.instance().getIp(), epRule);
        consumer.run(1);
        dataConsumer = consumer;
        logger.info("------------- Consumer was created from incoming z-node data ------------- ");

        if (ttl != null) {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                for (String children : AppData.instance().getZkSession().getChildren().forPath(path)) {
                                    logger.info("Our children is: " + children);
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
        logger.info("------------- Producer was created from incoming z-node data -------------------- ");
    }
}
