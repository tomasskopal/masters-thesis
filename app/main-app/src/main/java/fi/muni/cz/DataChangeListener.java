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
        switch (ActionType.valueOf((String) json.get("action"))) {
            case CREATE:
                createProducer((String)json.get("parent"));
                if (json.get("appMode").equals("combined")) {
                    createConsumer();
                }
                break;
        }
    }

    private void createConsumer() {
        dataConsumer = new Consumer("group-id", AppData.instance().getIp(), null);
        dataConsumer.run(1);
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
