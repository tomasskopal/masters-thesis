package fi.muni.cz;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+Producer+Example
 */
public class DataProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DataProducer.class);

    private String host;
    private String topic;
    private String identifier;

    public DataProducer(String host, String topic, String identifier) {
        this.host = host;
        this.topic = topic;
        this.identifier = identifier;
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("metadata.broker.list", host + ":9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<>(config);

        try {
            logger.info("Start sending data to topic: " + topic);

            int counter = 0;
            while (counter < 10) {
                String msg = "Severity:Level1, identifier:" + identifier;
                KeyedMessage<String, String> data = new KeyedMessage<>(topic, msg);
                producer.send(data);
                logger.info("MSG: " + msg);

                if (counter % 2 == 0) {
                    msg = "Severity:Level2, identifier:" + identifier;
                    data = new KeyedMessage<>(topic, msg);
                    producer.send(data);
                    logger.info("MSG: " + msg);
                }

                counter++;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.info("Producer Ends");

        } catch (Exception ex) {
            logger.error("Sending data fails", ex);
        } finally {
            producer.close();
            Thread.currentThread().interrupt();
        }
    }

}
