package pixlze.guildapi.features;

import pixlze.guildapi.features.list.AspectListFeature;
import pixlze.guildapi.features.list.TomeListFeature;

public class FeatureManager {
//    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();

    public void init() {
        registerFeature(new GuildRaidFeature());
        registerFeature(new AspectListFeature());
        registerFeature(new TomeListFeature());
        registerFeature(new DiscordBridgeFeature());
        registerFeature(new AutoUpdateFeature());
    }

    private void registerFeature(Feature feature) {
//        FEATURES.put(feature, FeatureState.ENABLED);
        initializeFeature(feature);
    }

    private void initializeFeature(Feature feature) {
        feature.init();
    }
}
