package Company;

import Info.RailwayInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Math.abs;

public class Train extends Thread {
    Depot depot;
    HashMap<String, List<Item>> items;
    ConfigCompany configCompany;
    private boolean isOff;
    private String name;
    private int speed;
    private int timeToAmortization;
    private RailwayInfo railwayInfo;

    private Logger logger = Logger.getLogger(getClass().getName());

    public Train(String name, ConfigCompany conf, Depot depot, RailwayInfo railwayInfo) {
        this.isOff = false;
        this.name = name;
        this.configCompany = conf;
        this.items = new HashMap<>();
        this.depot = depot;
        this.speed = configCompany.getTrainInfo(name).getSpeed();
        this.timeToAmortization = configCompany.getTrainInfo(name).getTimeToAmortization();
        this.railwayInfo = railwayInfo;
    }

    public void load(Station station) throws InterruptedException {
        station.startProcess(this);
        for (var it : configCompany.getTrainInfo(name).getItemCapacity().entrySet()) {
            for (int i = 0; i < it.getValue(); i++) {
                Thread.sleep(configCompany.getItemInfo(it.getKey()).getTimeToLoad());
                items.computeIfAbsent(it.getKey(), (x) -> new ArrayList<>()).add(station.getWarehouse(it.getKey()).getItem());
            }
        }
        station.endProcess(this);
    }

    public void unload(Station station) throws InterruptedException {
        station.startProcess(this);
        for (var it : configCompany.getTrainInfo(name).getItemCapacity().entrySet()) {
            for (int i = 0; i < it.getValue(); i++) {
                Thread.sleep(configCompany.getItemInfo(it.getKey()).getTimeToUnload());
                Warehouse warehouse = station.getWarehouse(it.getKey());
                warehouse.addItem(items.get(it.getKey()).get(0));
            }
        }
        station.endProcess(this);
    }

    @Override
    public void run() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        while (abs(startTime - Calendar.getInstance().getTimeInMillis()) < timeToAmortization) {
            try {
                load(railwayInfo.getStationDepart());
                logger.info("Train " + Thread.currentThread() + " " + name + " start moving");
                railwayInfo.getFromFactoryToConsumer().getRailway();
                Thread.sleep((long) ((double) railwayInfo.getFromFactoryToConsumer().getDistance() / (double) speed + 0.5));
                railwayInfo.getFromFactoryToConsumer().trainLeaveFromRailway();
                logger.info("Train " + Thread.currentThread() + " " + name + " start unloading");
                unload(railwayInfo.getStationArrive());
                railwayInfo.getFromConsumerToFactory().getRailway();
                Thread.sleep((long) ((double) railwayInfo.getFromConsumerToFactory().getDistance() / (double) speed + 0.5));
                railwayInfo.getFromConsumerToFactory().trainLeaveFromRailway();
                logger.info("Train " + Thread.currentThread() + " " + name + " is back");
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.info("Train " + Thread.currentThread() + " " + name + " was stopped");
            }
        }
        depot.addNewTrain(name);
    }
}
