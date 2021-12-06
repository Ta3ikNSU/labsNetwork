package Info;

import Company.Railway;
import Company.Station;

public class RailwayInfo {
    private final Station stationDepart;
    private final Station stationArrive;
    private final Railway fromFactoryToConsumer;
    private final Railway fromConsumerToFactory;

    public RailwayInfo(Station stationDepart, Station stationArrive, Railway fromFactoryToConsumer, Railway fromConsumerToFactory) {
        this.stationDepart = stationDepart;
        this.stationArrive = stationArrive;
        this.fromFactoryToConsumer = fromFactoryToConsumer;
        this.fromConsumerToFactory = fromConsumerToFactory;
    }

    public Station getStationDepart() {
        return stationDepart;
    }

    public Station getStationArrive() {
        return stationArrive;
    }

    public Railway getFromFactoryToConsumer() {
        return fromFactoryToConsumer;
    }

    public Railway getFromConsumerToFactory() {
        return fromConsumerToFactory;
    }
}
