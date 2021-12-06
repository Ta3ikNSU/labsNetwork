package Company;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Warehouse {

    private final int capacity;

    private BlockingQueue<Item> items;

    public Warehouse(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayBlockingQueue<>(capacity);
    }

    public void addItem(Item item) throws InterruptedException {
        items.put(item);
    }

    public Item getItem() throws InterruptedException {
        return items.take();
    }
}
