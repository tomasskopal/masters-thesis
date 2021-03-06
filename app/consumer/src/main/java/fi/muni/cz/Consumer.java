package fi.muni.cz;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import fi.muni.cz.esper.EventListener;
import fi.muni.cz.esper.IncommingEvent;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private static final Logger logger = Logger.getLogger("consumer");

    private static final String LOCALHOST_ZK = "localhost:2181";
    private static final String GROUP_ID = "group-id";

    private final ConsumerConnector consumer;
    private final String topic;
    private  ExecutorService executor;
    private EPServiceProvider cep;
    private String epRule;

    private static List<SimpleConsumer> consumerThreads = new ArrayList<>();

    public Consumer(String a_topic, String epRule) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig());
        this.topic = a_topic;
        this.epRule = epRule;
        consumerThreads = new ArrayList<>();

        Configuration cepConfig = new Configuration();
        cepConfig.addEventType(IncommingEvent.class);
        cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);

        logger.info("Creating consumer for topic: " + topic + " and rule: " + epRule);
    }


    public void run(int a_numThreads) {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        executor = Executors.newFixedThreadPool(a_numThreads);
        EPRuntime epRuntime = this.getEsperRuntime(null);

        // now create an object to consume the messages
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            SimpleConsumer consumerThread = new SimpleConsumer(stream, epRuntime);
            this.consumerThreads.add(consumerThread);
            executor.submit(consumerThread);
            threadNumber++;
        }
        logger.info(threadNumber + " threads is running. On topic: " + topic);
        logger.info("For sure, test if it is terminated: " + isTerminated());
    }

    public void inactive() {
        consumerThreads.forEach((thread) -> thread.inactive());
        logger.info("Setting consumer inactive. Topic: " + topic);
    }

    public void setEpRule(String epRule) {
        this.epRule = epRule;
        EPRuntime epRuntime = this.getEsperRuntime(epRule);
        consumerThreads.forEach((thread) -> thread.setEpRuntime(epRuntime));
        logger.info("New esper rule was set. Rule: " + epRule + " Topic: " + topic);
    }

    public void stop() {
        consumerThreads.forEach((thread) -> thread.shouldExit());
        cep.destroy();
        executor.shutdownNow();
        consumer.shutdown();
        logger.info("Consumer was terminated. Topic: " + topic);
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    private ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", LOCALHOST_ZK);
        props.put("group.id", GROUP_ID);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }

    private EPRuntime getEsperRuntime(String epRule) {
        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatement = cepAdm.createEPL(epRule == null ? this.epRule : epRule);
        cepStatement.addListener(new EventListener(this.epRule));
        return cep.getEPRuntime();
    }

}
