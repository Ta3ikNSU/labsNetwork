package Company;

import Info.ItemInfo;
import Info.StationInfo;
import Info.TrainInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigCompany {
    private final HashMap<String, TrainInfo> trainsInfo;
    private final HashMap<String, ItemInfo> itemsInfo;
    private StationInfo stationInfo;
    private Logger logger = Logger.getLogger(getClass().getName());

    public ConfigCompany() {
        this.itemsInfo = new HashMap<>();
        this.trainsInfo = new HashMap<>();
    }

    public ArrayList<String> getItemNames() {
        return new ArrayList<>(itemsInfo.keySet());
    }

    public ArrayList<String> getTrainsNames() {
        return new ArrayList<>(trainsInfo.keySet());
    }

    public int getDepartureCapacity(String name) {
        if (itemsInfo.get(name) == null) throw new NullPointerException("Item not found");
        return itemsInfo.get(name).getDepartureCapacity();
    }

    public int getDestinationCapacity(String name) {
        if (itemsInfo.get(name) == null) throw new NullPointerException("Item not found");
        return itemsInfo.get(name).getDestinationCapacity();
    }

    public int getTrainsNum() {
        return trainsInfo.size();
    }

    public TrainInfo getTrainInfo(String name) {
        return trainsInfo.get(name);
    }

    public ItemInfo getItemInfo(String name) {
        return itemsInfo.get(name);
    }

    public void parse() {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new InputStreamReader(getClass().getResourceAsStream("/config.cfg")));
            JSONArray items = (JSONArray) jsonObject.get("items_info");
            JSONArray trains = (JSONArray) jsonObject.get("trains_info");
            JSONObject station = (JSONObject) jsonObject.get("station_info");

            for (var item : items) {
                JSONObject newItem = (JSONObject) item;
                itemsInfo.put((String) newItem.get("name"),
                        new ItemInfo(((Long) newItem.get("countFactory")).intValue(),
                                ((Long) newItem.get("countConsumer")).intValue(),
                                (String) newItem.get("name"),
                                ((Long) newItem.get("departureCapacity")).intValue(),
                                ((Long) newItem.get("destinationCapacity")).intValue(),
                                ((Long) newItem.get("timeToCreate")).intValue(),
                                ((Long) newItem.get("timeToConsumption")).intValue(),
                                ((Long) newItem.get("timeToLoad")).intValue(),
                                ((Long) newItem.get("timeToUnload")).intValue()));
            }

            for (var train : trains) {
                Map<String, Integer> itemCapacityMap = new HashMap<>();
                JSONObject newTrain = (JSONObject) train;
                JSONObject itemCapacity = (JSONObject) ((JSONObject) train).get("itemCapacity");
                for (var itemName : itemsInfo.keySet()) {
                    int capacity = ((Long) itemCapacity.get(itemName)).intValue();
                    itemCapacityMap.put(itemName, capacity);
                }

                trainsInfo.put((String) newTrain.get("name"),
                        new TrainInfo(itemCapacityMap,
                                ((Long) newTrain.get("speed")).intValue(),
                                ((Long) newTrain.get("timeToCreate")).intValue(),
                                ((Long) newTrain.get("timeToAmortization")).intValue()));
            }
            stationInfo = new StationInfo(
                    ((Long) station.get("distance")).intValue(),
                    ((Long) station.get("numTracksAtDeparture")).intValue(),
                    ((Long) station.get("numTracksAtDestination")).intValue(),
                    ((Long) station.get("countTracksFromDepToDest")).intValue(),
                    ((Long) station.get("countTracksFromDestToDep")).intValue()
            );
            logger.info("Config created successfully");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public StationInfo getStationInfo() {
        return stationInfo;
    }


}
