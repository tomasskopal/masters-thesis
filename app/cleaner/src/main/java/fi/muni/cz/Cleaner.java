package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by tomasskopal on 29.12.15.
 */
public class Cleaner {

    public static void main(String[] args) {
        try {
            CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
                    args[0],                                  //   server list
                    5000,                                    //   session timeout time
                    3000,                                    //   connection create timeout time
                    new ExponentialBackoffRetry(1000, 3)     //   retry strategy
            );
            curatorFramework.start();

            if (curatorFramework.checkExists().forPath("/root") != null) {
                System.out.println("clear");
                curatorFramework.delete()
                        .guaranteed()
                        .deletingChildrenIfNeeded()
                        .withVersion(-1)
                        .forPath("/root");
            }
        } catch (Exception ex) {
            System.out.println("Fail: " + ex);
        }
    }
}
