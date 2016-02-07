package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.nio.charset.StandardCharsets;

/**
 * Created by tomasskopal on 19.12.15.
 */
public class DataChangeListener implements NodeCacheListener {

    private static Logger logger;

    private NodeCache dataCache;
    private DataProducer dataProducer = null;
    private Consumer dataConsumer = null;
    private Consumer basicDataConsumer = null;

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
                    createProducer((String) json.get("parent"), (String) json.get("path"));
                }
                if (json.get("appMode").equals("consumer")) {
                    createConsumer(
                            Boolean.valueOf((String)json.get("isBasic")),
                            AnalyzingLevel.valueOf((String) json.get("level"))
                    );
                    MainApp.registerChildrenWatcher((String) json.get("path"));
                }
                break;
            case MOVE:
                CuratorFramework curatorFramework = AppData.instance().getZkSession();
                JSONObject data = new JSONObject();
                data.put("action", ActionType.CREATE.toString());
                data.put("appMode", "producer");
                data.put("isBasic", String.valueOf(false));
                data.put("path", json.get("path"));
                data.put("parent", json.get("parent"));

                MainApp.createNodeAndRegisterWatcher((String) json.get("path"));
                Thread.sleep(1000);
                curatorFramework.setData().forPath((String) json.get("path"), data.toString().getBytes());

                dataProducer.stop();
                curatorFramework.delete().forPath(dataCache.getCurrentData().getPath());
                break;
        }
    }

    private void createConsumer(boolean isBasic, AnalyzingLevel analyzingLevel) {
        Consumer consumer = new Consumer(AppData.instance().getIp(), null, analyzingLevel);
        consumer.run(1);
        if (isBasic) {
            basicDataConsumer = consumer;
        } else {
            dataConsumer = consumer;
        }
        logger.info("Consumer was created from incoming z-node data");
    }

    private void createProducer(String parent, String path) {
        dataProducer = new DataProducer(null, parent, path);
        Thread producer = new Thread(
            dataProducer
        );
        producer.start();
        logger.info("Producer was created from incoming z-node data");
    }
}
