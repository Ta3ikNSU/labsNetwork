package Info;

public class ItemInfo {
    private final int countFactory;
    private final int countConsumer;

    private final String name; // имя
    private final int departureCapacity; // размер склада на станции отправления
    private final int destinationCapacity; // размер склада на станции назначения
    private final int timeToCreate; // время производства единицы товара
    private final int timeToConsumption; // время потребления единицы товара
    private final int timeToLoad; //время на загрузку единицы товара
    private final int timeToUnload; // время на выгрузку единицы товара


    public ItemInfo(int countFactory, int countConsumer, String name, int departureCapacity, int destinationCapacity, int timeToCreate, int timeToСonsumption, int timeToLoad, int timeToUnload) {
        this.countFactory = countFactory;
        this.countConsumer = countConsumer;
        this.name = name;
        this.departureCapacity = departureCapacity;
        this.destinationCapacity = destinationCapacity;
        this.timeToCreate = timeToCreate;
        this.timeToConsumption = timeToСonsumption;
        this.timeToLoad = timeToLoad;
        this.timeToUnload = timeToUnload;
    }

    public int getDepartureCapacity() {
        return departureCapacity;
    }

    public int getDestinationCapacity() {
        return destinationCapacity;
    }

    public int getTimeToConsumption() {
        return timeToConsumption;
    }

    public int getTimeToCreate() {
        return timeToCreate;
    }

    public int getTimeToLoad() {
        return timeToLoad;
    }

    public int getTimeToUnload() {
        return timeToUnload;
    }

    public int getCountFactory() {
        return countFactory;
    }

    public int getCountConsumer() {
        return countConsumer;
    }
}
