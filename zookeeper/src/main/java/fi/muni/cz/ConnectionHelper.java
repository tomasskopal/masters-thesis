package fi.muni.cz;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tomasskopal on 12.12.15.
 */
public class ConnectionHelper {

    private ZooKeeper zk;

    public ZooKeeper connect(String hosts) throws IOException, InterruptedException {
        if (zk != null) {
            return zk;
        }

        final CountDownLatch connectedSignal = new CountDownLatch(1);
        zk = new ZooKeeper(hosts, 5, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        connectedSignal.await();
        return zk;
    }

    public void createGroup(String groupName)
            throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        zk.create(path,
                null, // data,
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
    }

    public String joinGroup(String groupName, String memberName, byte[] data) throws KeeperException, InterruptedException {
        if (zk == null) {
            throw new IllegalArgumentException("Instance of zookeeper is null. Please create it first (use connect method)");
        }
        String path = "/" + groupName + "/" + memberName + "-";
        String createdPath = zk.create(path,
                data,
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        return createdPath;
    }

    public void delete(String groupName)
            throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        try {
            List<String> children = zk.getChildren(path, false);
            for (String child : children) {
                zk.delete(path + "/" + child, -1);
            }
            zk.delete(path, -1);
        }
        catch (KeeperException.NoNodeException e) {
            System.out.printf("Group %s does not exist\n", groupName);
        }
    }
}
