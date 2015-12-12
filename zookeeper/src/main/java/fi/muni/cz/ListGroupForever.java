package fi.muni.cz;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by tomasskopal on 12.12.15.
 */
public class ListGroupForever {
    private ZooKeeper zooKeeper;
    private Semaphore semaphore = new Semaphore(1);

    public ListGroupForever(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public static void run(String hosts, String groupName) throws Exception {
        ZooKeeper zk = new ConnectionHelper().connect(hosts);
        new ListGroupForever(zk).listForever(groupName);
    }

    public void listForever(String groupName)
            throws KeeperException, InterruptedException {
        semaphore.acquire();
        while (true) {
            list(groupName);
            semaphore.acquire();
        }
    }

    private void list(String groupName) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        List<String> children = zooKeeper.getChildren(path, event -> {
            if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                semaphore.release();
            }
        });
        if (children.isEmpty()) {
            System.out.printf("No members in group %s\n", groupName);
            return;
        }
        Collections.sort(children);
        System.out.println(children);
        System.out.println("--------------------");
    }
}
