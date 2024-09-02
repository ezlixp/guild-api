package pixlze.pixutils.net.models;

public class CompletedRaidModel {
    private final String[] users;
    private final String raid;
    private final long timestamp;

    public CompletedRaidModel(String[] users, String raid, long timestamp) {
        this.users = users;
        this.raid = raid;
        this.timestamp = timestamp;
    }
}
