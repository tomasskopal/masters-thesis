package fi.muni.cz;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomasskopal on 13.12.15.
 */
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private CuratorFramework curatorFramework;
    private Map<String, String> uriToZnodePath;

    public MainApp(String nodeName) {
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

            if (curatorFramework.checkExists().forPath("/root") != null) {
                logger.info("clear");
                curatorFramework.delete()
                        .guaranteed()
                        .deletingChildrenIfNeeded()
                        .withVersion(-1)
                        .forPath("/root");
            }

            if (curatorFramework.checkExists().forPath("/root") == null) {
                logger.info("Root znode is not created. Lets create it.");
                curatorFramework.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath("/root");
            }

            String znodePath = curatorFramework
                    .create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/root/" + nodeName, "init".getBytes());
            uriToZnodePath.put("127.0.0.1:2181", znodePath);

            // register watcher
            NodeCache dataCache = new NodeCache(curatorFramework, "/root/" + nodeName);
            dataCache.getListenable().addListener(new DataChangeListener(dataCache));
            dataCache.start();

            Thread.sleep(1000);

            JSONObject data = new JSONObject();
            data.put("action", ActionType.CREATE.toString());
            data.put("parent", "127.0.0.1");
            curatorFramework.setData().forPath("/root/" + nodeName, data.toString().getBytes());

            while (true){}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        if (args.length < 1) {
//            logger.error("Please enter app name");
//            return;
//        }
        new MainApp("PC1");
    }

}
