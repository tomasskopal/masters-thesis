package fi.muni.cz;

import com.espertech.esper.client.EPRuntime;
import fi.muni.cz.esper.IncommingEvent;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tomasskopal on 27.09.15.
 */
public class SimpleConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);

    private KafkaStream m_stream;
    private String topic;
    private EPRuntime epRuntime;

    public SimpleConsumer(KafkaStream a_stream, String topic, EPRuntime epRuntime) {
        this.topic = topic;
        this.m_stream = a_stream;
        this.epRuntime = epRuntime;
    }

    public void run() {
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext()) {
            String msg = new String(it.next().message());
            //System.out.println("Topic " + topic + ": " + msg);
            IncommingEvent event = new IncommingEvent(msg);
            logger.info(event.toString());
            epRuntime.sendEvent(event);
        }
    }
}