package fi.muni.cz;

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

        topics.forEach((topic) -> {
            Consumer example = new Consumer(zooKeeper, groupId, topic);
            example.run(threads);
        });
    }

}
