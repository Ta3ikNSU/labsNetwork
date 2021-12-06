package Info;

import java.util.HashMap;
import java.util.Map;

public class TrainInfo {
    private final Map<String, Integer> itemCapacity;
    private final int speed;
    private final int timeToCreate;
    private final int timeToAmortization;

    public TrainInfo(Map<String, Integer> itemCapacity, int speed, int timeToCreate, int timeToAmortization) {
        this.itemCapacity = new HashMap<>(itemCapacity);
        this.speed = speed;
        this.timeToCreate = timeToCreate;
        this.timeToAmortization = timeToAmortization;
    }

    public Map<String, Integer> getItemCapacity() {
        return itemCapacity;
    }

    public int getSpeed() {
        return speed;
    }

    public int getTimeToCreate() {
        return timeToCreate;
    }

    public int getTimeToAmortization() {
        return timeToAmortization;
    }
}
