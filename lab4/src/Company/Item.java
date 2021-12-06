package Company;

import java.util.concurrent.atomic.AtomicInteger;

public class Item {
    private String name;
    private final int id;
    private final AtomicInteger counter = new AtomicInteger();

    public Item(String name) {
        this.name = name;
        this.id = counter.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
