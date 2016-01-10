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

            createWorker(json);
        }
        catch(ParseException pe){
            logger.error("Unable to parse data. Position: " + pe.getPosition() + ". Data: " + data);
            return;
        }
        logger.info("------------------------------");
    }

    private void createWorker(JSONObject json) {
        switch (ActionType.valueOf((String) json.get("action"))) {
            case CREATE:
                if (json.get("parent") != null) {
                    createProducer((String)json.get("parent"));
                } else {
                    createConsumer();
                }
        }
    }

    private void createConsumer() {
        Consumer consumer = new Consumer("group-id", AppData.instance().getIp(), null);
        consumer.run(1);
        logger.info("Consumer was created from incoming z-node data");
    }

    private void createProducer(String parent) {
        Thread producer = new Thread(
                new DataProducer(parent, parent, AppData.instance().getIp())
        );
        producer.start();
        logger.info("Producer was created from incoming z-node data");
    }
}
