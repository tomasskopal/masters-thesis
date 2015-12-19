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
public class DataProducer {

    private static final Logger logger = LoggerFactory.getLogger(DataProducer.class);

    private static final String DEFAULT_TOPIC = "topic1";
    private static final String DEFAULT_IDENTIFIER = "PC1";

    public static void main( String[] args ) {
        String topic = DEFAULT_TOPIC;
        String identifier = DEFAULT_IDENTIFIER;
        if (args.length > 0) {
            topic = args[0];
        }
        if (args.length > 1) {
            identifier = args[1];
        }
        Properties props = new Properties();
        props.put("metadata.broker.list", "147.251.43.129:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        try {
            ProducerConfig config = new ProducerConfig(props);

            Producer<String, String> producer = new Producer<>(config);

            System.out.println("Start sending data to topic: " + topic);

            int counter = 0;
            while (counter < 10) {
                String msg = "Severity:Level1, identifier:" + identifier;
                KeyedMessage<String, String> data = new KeyedMessage<>(topic, msg);
                producer.send(data);
                System.out.println("MSG: " + msg);

                if (counter % 2 == 0) {
                    msg = "Severity:Level2, identifier:" + identifier;
                    data = new KeyedMessage<>(topic, msg);
                    producer.send(data);
                    System.out.println("MSG: " + msg);
                }

                counter++;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Producer Ends");
            producer.close();
        } catch (Exception ex) {
            logger.error("Sending data fails", ex);
        }
    }
}
