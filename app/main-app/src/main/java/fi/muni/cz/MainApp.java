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
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.json.simple.JSONObject;
import scala.App;

import java.util.Arrays;


/**
 * Created by tomasskopal on 13.12.15.
 */
public class MainApp {

    private static Logger logger = Logger.getLogger("producer"); // default value

    public MainApp(String appMode, String parentIp, String zkList, boolean isBasic) {
        String ip = AppData.instance().getIp();

        logger.info("Input arguments: IP: " + ip + ", appMode: " + appMode + ", parentIP: " + parentIp);

        try {
            CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
                    zkList,                                  //  server list
                    5000,                                    //  session timeout time
                    3000,                                    //  connection create timeout time
                    new ExponentialBackoffRetry(1000, 3)     //  retry strategy
            );
            curatorFramework.start();
            AppData.instance().setZkSession(curatorFramework);

            if (curatorFramework.checkExists().forPath(AppData.ZK_ROOT) == null) {
                logger.info("Root znode is not created. Lets create it.");
                curatorFramework.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(AppData.ZK_ROOT);
            }

            // prepare data object
            JSONObject data = new JSONObject();
            data.put("action", ActionType.CREATE.toString());
            data.put("parent", parentIp);
            data.put("isBasic", String.valueOf(isBasic));
            data.put("level", "LEVEL1");

            // create main node
            createNodeAndRegisterWatcher(AppData.ZK_ROOT + "/" + ip);
            Thread.sleep(1000);
            registerChildrenWatcher(AppData.ZK_ROOT + "/" + ip);
            Thread.sleep(1000);

            if (appMode.equals("combined")) {
                data.put("appMode", "consumer");
                data.put("path", AppData.ZK_ROOT + "/" + ip);
                curatorFramework.setData().forPath(AppData.ZK_ROOT + "/" + ip, data.toString().getBytes());
                Thread.sleep(1000);
            }

            // create producer node
            createNodeAndRegisterWatcher(AppData.ZK_ROOT + "/" + parentIp + "/" + ip);
            Thread.sleep(1000);

            data.put("appMode", "producer");
            data.put("isBasic", String.valueOf(false));
            data.put("path", AppData.ZK_ROOT + "/" + parentIp + "/" + ip);
            curatorFramework.setData().forPath(AppData.ZK_ROOT + "/" + parentIp + "/" + ip, data.toString().getBytes());

            while (true){} // TODO: move to the separate thread and remove this endless loop

        } catch (Exception e) {
            logger.error("Main app fails. Error: ", e);
        }
    }

    public static void createNodeAndRegisterWatcher(String path) throws Exception {
        createNode(path);

        // register watcher
        CuratorFramework curatorFramework = AppData.instance().getZkSession();
        NodeCache dataCache = new NodeCache(curatorFramework, path);
        dataCache.getListenable().addListener(new DataChangeListener(dataCache));
        dataCache.start();
    }

    public static void createNode(String path) throws Exception {
        CuratorFramework curatorFramework = AppData.instance().getZkSession();
        if (curatorFramework.checkExists().forPath(path ) != null) {
            logger.info("Node: " + path + " already exists.");
            return;
        }
        curatorFramework.create()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, "init".getBytes());

        // register watcher
        NodeCache dataCache = new NodeCache(curatorFramework, path);
        dataCache.getListenable().addListener(new DataChangeListener(dataCache));
        dataCache.start();
    }

    public static void registerChildrenWatcher(String path) throws Exception {
        CuratorFramework curatorFramework = AppData.instance().getZkSession();

        // register watcher
        PathChildrenCache cache = new PathChildrenCache(curatorFramework, path, true);
        cache.getListenable().addListener(new ChildrenChangeListener());
        cache.start();

        logger.info("Children change watcher was registered for path: " + path);
    }

    public static void main(String[] args) {
        try {
            Options options = new Options();
            Option ipOpt = new Option("ip", true, "PC ip address. Required.");
            ipOpt.setRequired(true);
            Option zkListOpt = new Option("zklist", true, "All zk servers. Required.");
            zkListOpt.setRequired(true);
            Option modeOpt = new Option("m", true, "Mode of app. Required.");
            modeOpt.setRequired(true);
            Option parentIpOpt = new Option("p", true, "Target for produced data. Required.");
            parentIpOpt.setRequired(true);

            options.addOption(ipOpt);
            options.addOption(zkListOpt);
            options.addOption(modeOpt);
            options.addOption(parentIpOpt);
            options.addOption("isbasic", false, "Means if this consumer is basic one. Optional");
            options.addOption("help", false, "show help");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if(cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("main-app", options);
                return;
            }

            boolean isBasic = false;
            if (cmd.hasOption("isbasic")) {
                isBasic = true;
            }

            if (!(cmd.getOptionValue("m").equals("producer") || cmd.getOptionValue("m").equals("combined"))) {
                logger.error("Unknown app mode: " + cmd.getOptionValue("m"));
                return;
            }

            logger.info("Input arguments: " + Arrays.asList(args).toString());

            AppData.instance().setIp(cmd.getOptionValue("ip"));
            AppData.instance().setLogger(logger);

            new MainApp(
                    cmd.getOptionValue("m"),
                    cmd.getOptionValue("p"),
                    cmd.getOptionValue("zklist"),
                    isBasic
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
