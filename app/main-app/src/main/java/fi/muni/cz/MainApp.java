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
    private static final String ZK_ROOT = "/root";

    private CuratorFramework curatorFramework;

    public MainApp(String appMode, String parentIp) {
        String ip = AppData.instance().getIp();

        logger.info("Input arguments: IP: " + ip + ", appMode: " + appMode + ", parentIP: " + parentIp);

        try {
            curatorFramework = CuratorFrameworkFactory.newClient(
                    AppData.instance().getZkList(),          //  server list
                    5000,                                    //  session timeout time
                    3000,                                    //  connection create timeout time
                    new ExponentialBackoffRetry(1000, 3)     //  retry strategy
            );
            curatorFramework.start();

            if (curatorFramework.checkExists().forPath(ZK_ROOT) == null) {
                logger.info("Root znode is not created. Lets create it.");
                curatorFramework.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(ZK_ROOT);
            }

            // prepare data object
            JSONObject data = new JSONObject();
            data.put("action", ActionType.CREATE.toString());
            data.put("parent", parentIp);
            data.put("appMode", appMode);

            // create node
            createNodeAndRegisterWatcher(ZK_ROOT + "/" + ip);
            Thread.sleep(1000);

            curatorFramework.setData().forPath(ZK_ROOT + "/" + ip, data.toString().getBytes());

            while (true){}

        } catch (Exception e) {
            logger.error("Main app fails. Error: ", e);
        }
    }

    private void createNodeAndRegisterWatcher(String path) throws Exception {
        if (curatorFramework.checkExists().forPath(path ) == null) {
            curatorFramework.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path, "init".getBytes());
        }

        // register watcher
        NodeCache dataCache = new NodeCache(curatorFramework, path);
        dataCache.getListenable().addListener(new DataChangeListener(dataCache));
        dataCache.start();
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
            options.addOption("help", false, "show help");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if(cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("main-app", options);
                return;
            }

            if (!(cmd.getOptionValue("m").equals("producer") || cmd.getOptionValue("m").equals("combined"))) {
                logger.error("Unknown app mode: " + cmd.getOptionValue("m"));
                return;
            }

            logger.info("Input arguments: " + Arrays.asList(args).toString());

            AppData.instance().setIp(cmd.getOptionValue("ip"));
            AppData.instance().setZkList(cmd.getOptionValue("zklist"));
            AppData.instance().setLogger(logger);

            new MainApp(
                    cmd.getOptionValue("m"),
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
