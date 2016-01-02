package fi.muni.cz;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import fi.muni.cz.esper.EventListener;
import fi.muni.cz.esper.Utils;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tomasskopal on 26.09.15.
 */
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private static final String LOCALHOST_ZK = "localhost:2181";

    private final ConsumerConnector consumer;
    private final String topic;
    private  ExecutorService executor;
    private EPRuntime epRuntime; // should be as singelton somewhere //TODO: make it

    public Consumer(String a_zookeeper, String a_groupId, String a_topic, EPRuntime epRuntime) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(a_zookeeper, a_groupId));
        this.topic = a_topic;
        this.epRuntime = epRuntime != null ? epRuntime : getEsperRuntime();
    }


    public void run(int a_numThreads) {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        executor = Executors.newFixedThreadPool(a_numThreads);

        // now create an object to consume the messages
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new SimpleConsumer(stream, topic, epRuntime));
            threadNumber++;
        }
        logger.info(threadNumber + " threads is running. On topic: " + topic);
    }

    private ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper != null ? a_zookeeper : LOCALHOST_ZK);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }

    private EPRuntime getEsperRuntime() {
        EPServiceProvider cep = Utils.getServiceProvider();
        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatement = cepAdm.createEPL("select *, count(*) from "
                + "IncommingEvent(severity='Level1').win:time_batch(5) having count(*) > 3");
        cepStatement.addListener(new EventListener());
        return cep.getEPRuntime();
    }

}