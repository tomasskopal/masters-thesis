package fi.muni.cz;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import fi.muni.cz.esper.EventListener;
import fi.muni.cz.esper.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class Application {

    private static final String LOCALHOST_ZK = "localhost:2181";
    private static final int LOCALHOST_THREATS = 1;
    private static final String LOCALHOST_GROUP = "group1";

    private static final List<String> topics = Arrays.asList("topic1", "topic2");


    public static void main(String[] args) {
        String zooKeeper = LOCALHOST_ZK;
        String groupId = LOCALHOST_GROUP;
        int threads = LOCALHOST_THREATS;

        EPRuntime epRuntime = getEsperRuntime();

        topics.forEach((topic) -> {
            Consumer example = new Consumer(zooKeeper, groupId, topic, epRuntime);
            example.run(threads);
        });
    }

    private static EPRuntime getEsperRuntime() {
        EPServiceProvider cep = Utils.getServiceProvider();
        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatement = cepAdm.createEPL("select *, count(*) from "
                + "IncommingEvent(severity='Level1').win:time_batch(5) having count(*) > 3");
        cepStatement.addListener(new EventListener());
        return cep.getEPRuntime();
    }

}
