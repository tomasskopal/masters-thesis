package fi.muni.cz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

/**
 * Created by tomasskopal on 12.01.16.
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    /**
     * Just for testing. TODO: delete this class
     */
    public static void main(String[] args) {
        String source = "root/192.168.1.1/192.168.1.1";
        System.out.println(source.substring(0, source.lastIndexOf("/")));
        System.out.println(source.substring(source.lastIndexOf("/") + 1, source.length()));
       /* while (true) {
            logger.info(LocalTime.now().toString());
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */
    }
}
