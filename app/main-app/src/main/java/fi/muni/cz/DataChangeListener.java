package fi.muni.cz;

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

    private void evaluateData(JSONObject json) {
        logger.info("Evaluated action will be: " + json.get("action"));
        switch (ActionType.valueOf((String) json.get("action"))) {
            case CREATE:
                createProducer((String)json.get("parent"));
                if (json.get("appMode").equals("combined")) {
                    createConsumer(Boolean.valueOf((String)json.get("isBasic")), AnalyzingLevel.LEVEL1);
                }
                break;
            case MOVE:
                dataProducer.setTopic((String)json.get("parent"));
                if (json.get("appMode").equals("combined")) {
                    logger.info("Stopping old consumer and creating new one.");
                    dataConsumer.stop();
                    createConsumer(false, AnalyzingLevel.LEVEL2);
                }
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

    private void createProducer(String parent) {
        dataProducer = new DataProducer(parent, parent, AppData.instance().getIp());
        Thread producer = new Thread(
            dataProducer
        );
        producer.start();
        logger.info("Producer was created from incoming z-node data");
    }
}
