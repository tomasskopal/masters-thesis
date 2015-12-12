package fi.muni.cz;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tomasskopal on 12.12.15.
 */
public class TestApp {

    private static final Logger logger = LoggerFactory.getLogger(TestApp.class);

    public static void main(String[] args) {
        try {
            ConnectionHelper conn = new ConnectionHelper();
            conn.connect("127.0.0.1:2181");

            conn.createGroup("root");
            //conn.createGroup("root/PC1");
            ListGroupForever.run("127.0.0.1:2181", "root");

            //conn.delete("root/PC1");
            //conn.delete("root");
        } catch (Exception e) {
            logger.error("oh fail", e);
        }
    }
}
