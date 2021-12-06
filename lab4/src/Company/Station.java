package Company;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Station {

    private final int trackNumbers;
    private final Map<String, Warehouse> warehouses;
    private final BlockingQueue<Train> trains;

    public Station(int trackNumbers, Map<String, Warehouse> warehouses) {
        this.trackNumbers = trackNumbers;
        this.warehouses = warehouses;
        trains = new ArrayBlockingQueue<>(trackNumbers);
    }

    public void startProcess(Train train) throws InterruptedException {
        trains.put(train);
    }

    public void endProcess(Train train) {
        trains.remove(train);
    }

    public Warehouse getWarehouse(String name) {
        if (warehouses.get(name) == null) {
            throw new NullPointerException("Warehouse not found");
        }
        return (warehouses.get(name));
    }
}
