package Company;

import Info.RailwayInfo;
import Info.StationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TranportCompany {
    private ArrayList<Thread> consumersAndFactories;
    private Depot depot;

    public TranportCompany() {
        consumersAndFactories = new ArrayList<>();
    }

    public void startWork() {
        ConfigCompany configCompany = new ConfigCompany();
        configCompany.parse();
        Map<String, Warehouse> inputWarehouse = new HashMap<>();
        Map<String, Warehouse> outputWarehouse = new HashMap<>();

        for (var item : configCompany.getItemNames()) {
            Warehouse startWarehouse = new Warehouse(configCompany.getDepartureCapacity(item));
            Warehouse endWarehouse = new Warehouse(configCompany.getDestinationCapacity(item));
            inputWarehouse.put(item, startWarehouse);
            outputWarehouse.put(item, endWarehouse);

            for (int i = 0; i < configCompany.getItemInfo(item).getCountConsumer(); i++) {
                Consumer consumer = new Consumer(item, endWarehouse, configCompany.getItemInfo(item).getTimeToConsumption());
                consumersAndFactories.add(consumer);
                consumer.start();
            }

            for (int i = 0; i < configCompany.getItemInfo(item).getCountFactory(); i++) {
                Factory factory = new Factory(item, startWarehouse, configCompany.getItemInfo(item).getTimeToCreate());
                consumersAndFactories.add(factory);
                factory.start();
            }
        }

        StationInfo stationInfo = configCompany.getStationInfo();
        RailwayInfo railwayInfo = new RailwayInfo(
                new Station(stationInfo.getNumTracksAtDeparture(), inputWarehouse),
                new Station(stationInfo.getNumTracksAtDestination(), outputWarehouse),
                new Railway(stationInfo.getCountTracksFromDepToDest(), stationInfo.getDistance()),
                new Railway(stationInfo.getCountTracksFromDepToDest(), stationInfo.getDistance())
        );
        depot = new Depot(configCompany, railwayInfo);
        for (var train : configCompany.getTrainsNames()) {
            depot.addNewTrain(train);
        }
    }

    public void stopWork() {
        depot.stop();
        consumersAndFactories.forEach(Thread::interrupt);
        for (Thread consumersAndFactory : consumersAndFactories) {
            try {
                consumersAndFactory.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}