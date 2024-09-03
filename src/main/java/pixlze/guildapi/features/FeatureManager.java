package pixlze.guildapi.features;

public class FeatureManager {
//    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();

    public void init() {
        registerFeature(new GuildRaidFeature());
        registerFeature(new AspectListFeature());
        registerFeature(new TomeListFeature());
    }

    private void registerFeature(Feature feature) {
//        FEATURES.put(feature, FeatureState.ENABLED);
        initializeFeature(feature);
    }

    private void initializeFeature(Feature feature) {
        feature.init();
    }
}
