package Company;

import java.util.logging.Logger;

public class Factory extends Thread {
    public final Warehouse warehouse;
    private final String itemName;
    private final int timeForCreate;
    private Logger logger = Logger.getLogger(getClass().getName());

    public Factory(String itemName, Warehouse warehouse, int timeForCreate) {
        this.warehouse = warehouse;
        this.itemName = itemName;
        this.timeForCreate = timeForCreate;
        logger.info("New Factory was created");
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(timeForCreate);
                Item newItem = new Item(itemName);
                warehouse.addItem(newItem);

                logger.info("Factory creat goods: " + itemName);
            } catch (InterruptedException e) {
                e.printStackTrace();

                return;
            }
        }
        logger.info("Factory was stopped");
    }
}
