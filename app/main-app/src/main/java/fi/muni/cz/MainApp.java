package fi.muni.cz;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomasskopal on 13.12.15.
 */
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private static CuratorFramework curatorFramework;
    private static Map<String, String> uriToZnodePath;

    public static void init() {
        try {
            curatorFramework = CuratorFrameworkFactory.newClient(
                    "127.0.0.1:2181",                    //   server list
                    5000,                                    //   session timeout time
                    3000,                                    //   connection create timeout time
                    new ExponentialBackoffRetry(1000, 3)     //   retry strategy
            );
            curatorFramework.start();
            uriToZnodePath = new HashMap<>();
            //conn.connect("147.251.43.129:2181,147.251.43.130:2181");


            if (curatorFramework.checkExists().forPath("/PC1") != null) {
                logger.info("Root PC1 is already created. Clear it.");
                curatorFramework.delete()
                        .guaranteed()
                        .deletingChildrenIfNeeded()
                        .withVersion(-1)
                        .forPath("/PC1");
            }

            String znodePath = curatorFramework
                    .create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/PC1", "127.0.0.1:2181".getBytes());
            uriToZnodePath.put("127.0.0.1:2181", znodePath);

            znodePath = curatorFramework
                    .create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/PC1/PC1", "127.0.0.1:2181".getBytes());
            uriToZnodePath.put("127.0.0.1:2181", znodePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
