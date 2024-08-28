package pixlze.utils.requests;

public class CompletedRaidPojo {
    private final String[] users;
    private final String raid;
    private final long timestamp;

    public CompletedRaidPojo(String[] users, String raid, long timestamp) {
        this.users = users;
        this.raid = raid;
        this.timestamp = timestamp;
    }
}
