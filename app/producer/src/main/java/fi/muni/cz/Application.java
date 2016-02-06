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
        while (true) {
            logger.info(LocalTime.now().toString());
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
