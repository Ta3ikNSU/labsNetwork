package Company;

import Info.RailwayInfo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Depot {
    private final ConfigCompany configure; // конфигурация всего
    private final RailwayInfo railways;
    private Queue<Train> trains; // поезда
    private ScheduledExecutorService executor;
    private Logger logger = Logger.getLogger(getClass().getName());

    public Depot(ConfigCompany configure, RailwayInfo railways) {
        this.railways = railways;
        this.trains = new ConcurrentLinkedQueue<>();
        this.executor = Executors.newScheduledThreadPool(configure.getTrainsNum());
        this.configure = configure;
        logger.info("New depot was created");
    }

    public void addNewTrain(String name) {
        Depot depot = this;
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Train newTrain = new Train(name, configure, depot, railways);
                logger.info("New train was created");
                trains.add(newTrain);
                newTrain.start();
                logger.info("New train start work");
            }
        }, configure.getTrainInfo(name).getTimeToCreate(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executor.shutdownNow();
        trains.forEach(Thread::interrupt);
        for (Train train : trains) {
            try {
                train.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("All train was stopped");
    }
}

