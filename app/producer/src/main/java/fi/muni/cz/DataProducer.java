package fi.muni.cz;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+Producer+Example
 */
public class DataProducer implements Runnable {

    private static final Logger logger = Logger.getLogger("producer");

    boolean shutdown = false;

    private String host = "147.251.43.181";
    private AnalyzingLevel level;
    private String topic;
    private String identifier;

    public DataProducer(String level, String topic, String identifier) {
        this.level = AnalyzingLevel.valueOf(level);
        this.topic = topic;
        this.identifier = identifier;
    }

    @Override
    public void run() {
        sendData();
    }

    private void sendData() {
        Properties props = new Properties();
        props.put("metadata.broker.list", host + ":9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<>(config);

        final boolean getRandom = true;

        try {
            logger.info("Start sending data to topic: " + topic);


            long counter = 0;

            while (!shutdown) {
                JSONObject dataMsg = getData();

                KeyedMessage<String, String> data = new KeyedMessage<>(topic, dataMsg.toString());
                if (identifier.endsWith("130") || identifier.endsWith("181")) {
                    producer.send(data);
                    logger.info("MSG: " + dataMsg.toString() + ", to topic: " + topic + ", from: " + identifier);
                }


                //if (counter % 10 == 0) {
                //    logger.info("MSG: " + dataMsg.toString() + ", to topic: " + topic + ", from: " + identifier);
                //}

                try {
                    Thread.sleep(this.level.equals(AnalyzingLevel.LEVEL1) ? 1000 : (ThreadLocalRandom.current().nextInt(3, 10) * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }

        } catch (Exception ex) {
            logger.error("Sending data fails", ex);
        }
    }

    private JSONObject getData() {
        JSONObject dataMsg = new JSONObject();
        dataMsg.put("msg", "Message from the data producer.");
        dataMsg.put("level", this.level.toString());
        dataMsg.put("source", this.identifier);

        switch (this.identifier.substring(this.identifier.length() - 3)) {
            case "181":
                dataMsg.put("flag", "SYN");
                dataMsg.put("size", "10");
                dataMsg.put("port", "80");
                break;
            case "130":
                dataMsg.put("flag", "SYN");
                dataMsg.put("port", "11");
                dataMsg.put("size", "10");
                break;
            case "138":
                dataMsg.put("flag", "ACK");
                dataMsg.put("port", "11");
                dataMsg.put("size", "100");
                break;
            case "150":
                dataMsg.put("flag", "ACK");
                dataMsg.put("port", "80");
                dataMsg.put("size", "100");
                break;
        }
        return dataMsg;
    }

    public void stop() {
        logger.info("Stopping producer for topic: " + topic);
        this.shutdown = true;
    }
}
