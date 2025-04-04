package pixlze.guildapi.core.features;

import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Manager;
import pixlze.guildapi.features.AutoUpdateFeature;
import pixlze.guildapi.features.GuildRaidFeature;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeatureManager extends Manager {
    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();
    private static final Map<Class<? extends Feature>, Feature> FEATURE_INSTANCES = new LinkedHashMap<>();

    public void init() {
        if (GuildApi.isTesting())
            registerFeature(new GuildRaidFeature());
        registerFeature(new DiscordBridgeFeature());
        registerFeature(new AutoUpdateFeature());
    }

    private void registerFeature(Feature feature) {
        initializeFeature(feature);
    }

    private void initializeFeature(Feature feature) {
        feature.init();
        FEATURES.put(feature, FeatureState.ENABLED);
        FEATURE_INSTANCES.put(feature.getClass(), feature);
    }

    public void enableFeature(Feature feature) {
        FEATURES.put(feature, FeatureState.ENABLED);
        feature.onEnabled();
    }

    public void disableFeature(Feature feature) {
        FEATURES.put(feature, FeatureState.DISABLED);
        feature.onDisabled();
    }

    public List<Feature> getFeatures() {
        return FEATURES.keySet().stream().toList();
    }

    public Feature getFeatureInstance(Class<? extends Feature> feature) {
        return FEATURE_INSTANCES.get(feature);
    }

    public FeatureState getFeatureState(Feature feature) {
        if (!FEATURES.containsKey(feature))
            GuildApi.LOGGER.warn("tried to get feature state of unregistered feature: {}", feature);
        return FEATURES.getOrDefault(feature, FeatureState.DISABLED);
    }
}
