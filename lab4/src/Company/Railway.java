package Company;

public class Railway {
    private final int capacity; // количество поездов, которые могут одновременно двигаться по этой жд
    private final int distance; // длина пути
    private volatile int countCurTrain; //текущее количество поездов на жд

    public Railway(int capacity, int distance) {
        this.capacity = capacity;
        this.countCurTrain = 0;
        this.distance = distance;
    }

    //Добавление поезда на путь
    public synchronized void getRailway() throws InterruptedException {
        while (capacity <= countCurTrain) {
            wait();
        }
        countCurTrain++;
    }

    public synchronized void trainLeaveFromRailway() {
        if (countCurTrain == 0) {
            throw new IllegalStateException("Trains on this road are out ( " + this.hashCode() + " )");
        }
        countCurTrain--;
        notifyAll();
    }

    public int getDistance() {
        return distance;
    }
}
