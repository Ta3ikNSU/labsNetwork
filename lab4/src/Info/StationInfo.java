package Info;

public class StationInfo {
    private final int distance; // расстояние между станциями
    private final int numTracksAtDeparture; // количество путей на станции отправления
    private final int numTracksAtDestination; // количество путей на станции назначения
    private final int countTracksFromDepToDest; // количество путей от станции отправления до станции назначения
    private final int countTracksFromDestToDep; //количество путей от станции назначения до станции отправления

    public StationInfo(int distance, int numTracksAtDeparture, int numTracksAtDestination, int countTracksFromDepToDest, int countTracksFromDestToDep) {
        this.distance = distance;
        this.numTracksAtDeparture = numTracksAtDeparture;
        this.numTracksAtDestination = numTracksAtDestination;
        this.countTracksFromDepToDest = countTracksFromDepToDest;
        this.countTracksFromDestToDep = countTracksFromDestToDep;
    }

    public int getDistance() {
        return distance;
    }

    public int getNumTracksAtDeparture() {
        return numTracksAtDeparture;
    }

    public int getNumTracksAtDestination() {
        return numTracksAtDestination;
    }

    public int getCountTracksFromDepToDest() {
        return countTracksFromDepToDest;
    }

    public int getCountTracksFromDestToDep() {
        return countTracksFromDestToDep;
    }
}
