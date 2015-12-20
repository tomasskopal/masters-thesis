package fi.muni.cz;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Created by tomasskopal on 19.12.15.
 */
public class DataChangeListener implements NodeCacheListener {

    private static final Logger logger = LoggerFactory.getLogger(DataChangeListener.class);

    private NodeCache dataCache;

    public DataChangeListener(NodeCache dataCache) {
        this.dataCache = dataCache;
    }

    @Override
    public void nodeChanged() throws Exception {
        String data = new String(dataCache.getCurrentData().getData(), StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();

        try{
            JSONObject json = (JSONObject) parser.parse(data);
            createWorker(json);
        }
        catch(ParseException pe){
            logger.error("Unable to parse data. Position: " + pe.getPosition() + ". Data: " + data);
            return;
        }
        logger.info(dataCache.getCurrentData().getPath());
        logger.info("------------------------------");
    }

    private void createWorker(JSONObject json) {
        switch (ActionType.valueOf((String) json.get("action"))) {
            case CREATE:
                if (json.get("parent") != null) {
                    createProducer((String)json.get("parent"));
                }
        }
    }

    private void createProducer(String parent) {
        Thread producer = new Thread(
                new DataProducer(parent, "topic1", "PC1")
        );
        producer.start();
    }
}
