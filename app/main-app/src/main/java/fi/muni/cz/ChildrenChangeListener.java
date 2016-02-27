package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.log4j.Logger;

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

    private Set<String> childrens = new HashSet<>();

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        String path = event.getData().getPath();
        switch (event.getType()) {
            case CHILD_ADDED: {
                logger.info("Node added: " + path);
                childrens.add(path);
                break;
            }

            case CHILD_REMOVED: {
                logger.info("Node removed: " + path);
                childrens.remove(path);

                if (childrens.isEmpty()) {
                    String[] pathSplit = path.split("/");
                    if (pathSplit.length != 3) {
                        logger.warn("Path has no 3 parts. path: " + path);
                    }
                    terminateConsumer(pathSplit[1]);
                }
                break;
            }
            default:
                break;
        }
    }

    private void terminateConsumer (String path) {
        logger.info("Terminating consumer for path: " + path);
    }
}
