package fi.muni.cz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.log4j.Logger;

/**
 * Created by tomasskopal on 02.01.16.
 */
public class AppData {

    public static final String ZK_ROOT = "/root";

    private static AppData instance;

    private String ip;
    private CuratorFramework zkSession;
    private Logger logger;

    private AppData() {}

    public static AppData instance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public CuratorFramework getZkSession() {
        return zkSession;
    }

    public void setZkSession(CuratorFramework zkSession) {
        this.zkSession = zkSession;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
