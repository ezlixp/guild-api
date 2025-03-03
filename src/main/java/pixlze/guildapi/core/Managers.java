package pixlze.guildapi.core;


import pixlze.guildapi.core.features.FeatureManager;
import pixlze.guildapi.net.NetManager;

public final class Managers {
    public static final NetManager Net = new NetManager();

    public static final FeatureManager Feature = new FeatureManager();

    public static void init() {
        Net.init();
        Feature.init();
    }
}
