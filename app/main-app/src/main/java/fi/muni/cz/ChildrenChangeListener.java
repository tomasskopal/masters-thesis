package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tomasskopal on 07.02.16.
 */
public class ChildrenChangeListener implements PathChildrenCacheListener {

    private static Logger logger;

    public ChildrenChangeListener() {
        logger = AppData.instance().getLogger();
    }

    private Set<String> children = new HashSet<>();

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        String path = event.getData().getPath();
        switch (event.getType()) {
            case CHILD_ADDED: {
                logger.info("Node added: " + path);
                children.add(path);
                break;
            }

            case CHILD_REMOVED: {
                logger.info("Node removed: " + path);
                children.remove(path);

                if (children.isEmpty()) {
                    String[] pathSplit = path.split("/");
                    if (pathSplit.length != 4) { // it starts with "/"
                        logger.warn("Path has no 3 parts. path: " + path);
                    }
                    terminateConsumer(pathSplit[2]);
                }
                break;
            }
            default:
                break;
        }
    }

    private void terminateConsumer (String node) {
        try {
            logger.info("Terminating consumer for node: " + node);

            JSONObject data = new JSONObject();
            data.put("action", ActionType.STOP_CONSUMER.toString());
            AppData.instance().getZkSession().setData().forPath(AppData.ZK_ROOT + "/" + node, data.toString().getBytes());
        } catch (Exception e) {
            logger.error("Something went wrong when watcher try to stop consumer.", e);
        }
    }
}
