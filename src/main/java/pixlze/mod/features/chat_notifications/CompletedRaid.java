package pixlze.mod.features.chat_notifications;

public class CompletedRaid {
    private String[] igns;
    private String raid;
    private long timestamp;

    public CompletedRaid(String[] igns, String raid, long timestamp) {
        this.igns = igns;
        this.raid = raid;
        this.timestamp = timestamp;
    }
}
