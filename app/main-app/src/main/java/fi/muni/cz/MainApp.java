package fi.muni.cz;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by tomasskopal on 13.12.15.
 */
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static final String ZK_ROOT = "/root";

    private CuratorFramework curatorFramework;

    public MainApp(String ip, String zkPath, String zkList, AppMode appMode, String parentIp) {
        try {
            curatorFramework = CuratorFrameworkFactory.newClient(
                    zkList,                                  //   server list
                    5000,                                    //   session timeout time
                    3000,                                    //   connection create timeout time
                    new ExponentialBackoffRetry(1000, 3)     //   retry strategy
            );
            curatorFramework.start();

            /*
            if (curatorFramework.checkExists().forPath("/root") != null) {
                logger.info("clear");
                curatorFramework.delete()
                        .guaranteed()
                        .deletingChildrenIfNeeded()
                        .withVersion(-1)
                        .forPath("/root");
            }
            */

            if (curatorFramework.checkExists().forPath(ZK_ROOT) == null) {
                logger.info("Root znode is not created. Lets create it.");
                curatorFramework.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(ZK_ROOT);
            }

            JSONObject data = new JSONObject();

            if (appMode == AppMode.COMBINED) {
                // create node
                createNodeAndRegisterWatcher(ZK_ROOT + "/" + ip, ip);
                Thread.sleep(1000);

                // dispatch action - consumer
                data.put("action", ActionType.CREATE.toString());
                curatorFramework.setData().forPath(ZK_ROOT + "/" + ip, data.toString().getBytes());
                Thread.sleep(1000);
            }

            // create node
            createNodeAndRegisterWatcher(ZK_ROOT + zkPath + "/" + ip, ip);
            Thread.sleep(1000);

            // dispatch action - producer
            data = new JSONObject();
            data.put("action", ActionType.CREATE.toString());
            data.put("parent", parentIp);
            curatorFramework.setData().forPath(ZK_ROOT + zkPath + "/" + ip, data.toString().getBytes());

            while (true){}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNodeAndRegisterWatcher(String path, String ip) throws Exception {
        curatorFramework.create()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, "init".getBytes());

        // register watcher
        NodeCache dataCache = new NodeCache(curatorFramework, path);
        dataCache.getListenable().addListener(new DataChangeListener(dataCache, ip));
        dataCache.start();
    }

    public static void main(String[] args) {
        try {
            Options options = new Options();
            Option ipOpt = new Option("ip", true, "PC ip address. Required.");
            ipOpt.setRequired(true);
            Option zkPathOpt = new Option("zkpath", true, "Place at zk-tree where pc have to include in. Required.");
            zkPathOpt.setRequired(true);
            Option zkListOpt = new Option("zklist", true, "All zk servers. Required.");
            zkPathOpt.setRequired(true);
            Option modeOpt = new Option("m", true, "Mode of app. Required.");
            modeOpt.setRequired(true);

            options.addOption(ipOpt);
            options.addOption(zkPathOpt);
            options.addOption(zkListOpt);
            options.addOption("p", true, "Target for produced data. Optional");
            options.addOption("help", false, "show help");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse( options, args);

            if(cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("main-app", options);
                return;
            }

            if (!cmd.getOptionValue("zkpath").startsWith("/")) {
                System.out.println("Zk path have to starts with '/'");
            }

            new MainApp(
                    cmd.getOptionValue("ip"),
                    cmd.getOptionValue("zkpath"),
                    cmd.getOptionValue("zklist"),
                    AppMode.valueOf(cmd.getOptionValue("m").toUpperCase()),
                    cmd.getOptionValue("p")
            );

        } catch (ParseException e) {
            if (e instanceof MissingOptionException) {
                System.out.println("Missing cmd options: " + ((MissingOptionException) e).getMissingOptions());
                return;
            }
            logger.error("Parameters parsing crashed " + e);
        }
    }

}
