package fi.muni.cz;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

/**
 * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+Producer+Example
 */
public class App {

    public static void main( String[] args ) {

        Properties props = new Properties();

        props.put("metadata.broker.list", "192.168.0.21:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);

        Producer<String, String> producer = new Producer<>(config);

        //sending...
        String topic = "data_collect_events";
        String message = "_Message_1";
        KeyedMessage<String, String> keyedMessage = new KeyedMessage<>(topic, message);
        producer.send(keyedMessage);

        producer.close();

        System.out.println( "Hello World!" );
    }
}
