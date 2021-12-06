package Company;

import java.util.logging.Logger;

public class Consumer extends Thread {

    public final Warehouse warehouse;
    private final String itemName;
    private final int timeForConsume;
    private Logger logger = Logger.getLogger(getClass().getName());

    public Consumer(String itemName, Warehouse warehouse, int timeForConsume) {
        this.warehouse = warehouse;
        this.itemName = itemName;
        this.timeForConsume = timeForConsume;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(timeForConsume);
                warehouse.getItem();
                logger.info("Consumer took the good: " + itemName + ";");
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.info("Consumer: " + itemName + " was stop;");
                e.printStackTrace();
                return;
            }
        }
    }
}
