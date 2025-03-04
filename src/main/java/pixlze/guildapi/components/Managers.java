package pixlze.guildapi.components;


import pixlze.guildapi.consumers.FeatureManager;
import pixlze.guildapi.mod.ConnectionManager;
import pixlze.guildapi.net.NetManager;

public final class Managers {
    public static final ConnectionManager Connection = new ConnectionManager();
    public static final NetManager Net = new NetManager();

    public static final FeatureManager Feature = new FeatureManager();
}
