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

    private static final int LOCALHOST_THREATS = 1;
    private static final String LOCALHOST_GROUP = "group-id";

    private static final List<String> topics = Arrays.asList("147.251.43.129");


    public static void main(String[] args) {
        String groupId = LOCALHOST_GROUP;
        int threads = LOCALHOST_THREATS;

        topics.forEach((topic) -> {
            Consumer example = new Consumer(groupId, topic, null);
            example.run(threads);
        });
    }



}
