package pixlze.guildapi.core.features;

import pixlze.guildapi.core.Manager;
import pixlze.guildapi.features.AutoUpdateFeature;
import pixlze.guildapi.features.GuildRaidFeature;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeatureManager extends Manager {
    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();

    public void init() {
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
}
