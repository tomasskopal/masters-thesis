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

    volatile boolean shutdown = false;

    private String host;
    private String topic;
    private String identifier;

    public DataProducer(String host, String topic, String identifier) {
        this.host = host != null ? host : "147.251.43.129";
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

        // TODO : remove whitelist
        boolean getRandomNumber = true;
        if (identifier.contains("147.251.43.150") || identifier.contains("147.251.43.138")) {
            getRandomNumber = false;
        }
        final boolean getRandom = getRandomNumber;

        try {
            logger.info("Start sending data to topic: " + topic);

            Timer timer = new java.util.Timer();
            timer.scheduleAtFixedRate( // send  more 10 random messages every 10 seconds
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        for (int i=0; i<10; i++) {
                            JSONObject dataMsg = new JSONObject();
                            dataMsg.put("msg", "Some random text with message");
                            dataMsg.put("level", getRandom ? String.valueOf(ThreadLocalRandom.current().nextInt(1, 3)) : "2");
                            dataMsg.put("source", identifier);

                            KeyedMessage<String, String> data = new KeyedMessage<>(topic, dataMsg.toString());
                            producer.send(data);
                        }
                        logger.info("Bunch of errors send.");
                    }
                },
                0, // delay for run at the first time
                10000 // period
            );

            while (!shutdown) {
                JSONObject dataMsg = new JSONObject();
                dataMsg.put("msg", "Some random text with message");
                dataMsg.put("level", getRandom ? String.valueOf(ThreadLocalRandom.current().nextInt(1, 3)) : "2");
                dataMsg.put("source", identifier);

                KeyedMessage<String, String> data = new KeyedMessage<>(topic, dataMsg.toString());
                producer.send(data);
                //logger.info("MSG: " + dataMsg.toString() + ", to topic: " + topic);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {
            logger.error("Sending data fails", ex);
        }
    }

    public void stop() {
        this.shutdown = true;
    }
}
