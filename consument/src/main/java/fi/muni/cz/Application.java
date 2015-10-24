package fi.muni.cz;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class Application {

    private static final String LOCALHOST_ZK = "localhost:2181";
    private static final String LOCALHOST_TOPIC = "page_visits";
    private static final int LOCALHOST_THREATS = 1;
    private static final String LOCALHOST_GROUP = "group1";


    public static void main(String[] args) {
        String zooKeeper = LOCALHOST_ZK;
        String groupId = LOCALHOST_GROUP;
        String topic = LOCALHOST_TOPIC;
        int threads = LOCALHOST_THREATS;

        Consumer example = new Consumer(zooKeeper, groupId, topic);
        example.run(threads);
    }

}
