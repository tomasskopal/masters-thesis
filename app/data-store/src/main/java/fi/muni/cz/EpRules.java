package fi.muni.cz;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomasskopal on 27.02.16.
 */
public class EpRules {

    private static EpRules instance;

    private static Map<String, String> rules = new HashMap<>();

    private EpRules() {
        rules.put("SYN_FLOOD", "select source, count(*) as cnt, level from "
                + "IncommingEvent(level='LEVEL1').win:time_batch(5 sec) WHERE flag = 'SYN' group by source, level having count(*) > 3");

        rules.put("LEVEL2", "select source, count(*) as cnt, level from "
                + "IncommingEvent(level='LEVEL2').win:time_batch(5 sec) group by source, level having count(*) > 7");
    }

    public static EpRules instance() {
        if (instance == null) {
            instance = new EpRules();
        }
        return instance;
    }

    public String getRule(String key) {
        return rules.get(key);
    }
}
