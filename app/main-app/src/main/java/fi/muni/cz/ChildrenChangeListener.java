package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.log4j.Logger;

/**
 * Created by tomasskopal on 07.02.16.
 */
public class ChildrenChangeListener implements PathChildrenCacheListener {

    private static Logger logger;

    public ChildrenChangeListener() {
        logger = AppData.instance().getLogger();
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        switch (event.getType()) {
            case CHILD_ADDED: {
                logger.info("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                break;
            }

            case CHILD_UPDATED: {
                logger.info("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                break;
            }

            case CHILD_REMOVED: {
                logger.info("Node removed: "    + ZKPaths.getNodeFromPath(event.getData().getPath()));
                break;
            }
            default:
                break;
        }
        logger.info("Node added: " + event.getData().toString());
    }
}
