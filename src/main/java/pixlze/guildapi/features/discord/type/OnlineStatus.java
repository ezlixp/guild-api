package pixlze.guildapi.features.discord.type;

public enum OnlineStatus {
    ONLINE(0),
    BUSY(1),
    DND(2),
    INVISIBLE(3);


    public final int value;

    OnlineStatus(int value) {
        this.value = value;
    }
}
