package fi.muni.cz;

/**
 * Created by tomasskopal on 02.01.16.
 */
public class AppData {

    private static AppData instance;

    private String ip;
    private String zkList;

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

    public String getZkList() {
        return zkList;
    }

    public void setZkList(String zkList) {
        this.zkList = zkList;
    }
}
