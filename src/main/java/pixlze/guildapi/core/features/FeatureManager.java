package pixlze.guildapi.core.features;

import pixlze.guildapi.core.Manager;
import pixlze.guildapi.features.AutoUpdateFeature;
import pixlze.guildapi.features.GuildRaidFeature;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;

public class FeatureManager extends Manager {
//    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();

    public void init() {
//        registerFeature(new ClientCommandHelpFeature());
//        if (GuildApi.isTesting()) registerFeature(new TestCommandHelpFeature());

        registerFeature(new GuildRaidFeature());
//        registerFeature(new RaidRewardsListClientCommand());
//        registerFeature(new TomeListClientCommand());

        registerFeature(new DiscordBridgeFeature());
//        registerFeature(new DiscordBlockClientCommand());

        registerFeature(new AutoUpdateFeature());
    }

    private void registerFeature(Feature feature) {
//        FEATURES.put(feature, FeatureState.ENABLED);
        initializeFeature(feature);
    }

    private void initializeFeature(Feature feature) {
        feature.init();
    }

    public void enableFeature(Feature feature) {
        feature.onEnabled();
    }

    public void disableFeature(Feature feature) {
        feature.onDisabled();
    }
}
