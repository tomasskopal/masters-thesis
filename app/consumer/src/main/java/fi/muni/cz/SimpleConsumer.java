package fi.muni.cz;

import com.espertech.esper.client.EPRuntime;
import fi.muni.cz.esper.IncommingEvent;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.log4j.Logger;

/**
 * Created by tomasskopal on 27.09.15.
 */
public class SimpleConsumer implements Runnable {

    private static final Logger logger = Logger.getLogger("consumer");

    private KafkaStream m_stream;
    private EPRuntime epRuntime;
    private boolean inactive = false;
    private boolean isExit = false;

    public SimpleConsumer(KafkaStream a_stream, EPRuntime epRuntime) {
        this.m_stream = a_stream;
        this.epRuntime = epRuntime;
        logger.info("Simple consumer was created and inactive is " + inactive);
    }

    public void inactive() {
        this.inactive = true;
    }

    public void shouldExit() {
        this.isExit = true;
    }


    public void setEpRuntime(EPRuntime epRuntime) {
        this.inactive = false;
        this.epRuntime = epRuntime;
    }

    public void run() {
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext()) {
            if (isExit) {
                break;
            }
            if (this.inactive) {
                continue;
            }
            String msg = new String(it.next().message());
            //if (AppData.instance().getIp().endsWith("130")) {
                logger.info("Message received: " + msg);
            //}
            IncommingEvent event = new IncommingEvent(msg);
            this.epRuntime.sendEvent(event);
        }
    }
}