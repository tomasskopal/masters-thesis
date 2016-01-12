package fi.muni.cz;

/**
 * Created by tomasskopal on 12.01.16.
 */
public class Application {

    /**
     * Just for testing. TODO: delete this class
     */
    public static void main(String[] args) {
        DataProducer dataProducer = new DataProducer("147.251.43.129", "147.251.43.129", "147.251.43.129");
        Thread producer = new Thread(
                dataProducer
        );
        producer.start();
    }
}
