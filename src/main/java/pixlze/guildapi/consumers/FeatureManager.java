package pixlze.guildapi.consumers;

import pixlze.guildapi.components.Feature;
import pixlze.guildapi.features.AutoUpdateFeature;
import pixlze.guildapi.features.CommandHelpFeature;
import pixlze.guildapi.features.discord.DiscordBlockFeature;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;
import pixlze.guildapi.features.guildresources.GuildRaidFeature;
import pixlze.guildapi.features.guildresources.RaidRewardsListFeature;
import pixlze.guildapi.features.guildresources.TomeListFeature;

public class FeatureManager {
//    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();

    public void init() {
        registerFeature(new CommandHelpFeature());

        registerFeature(new GuildRaidFeature());
        registerFeature(new RaidRewardsListFeature());
        registerFeature(new TomeListFeature());

        registerFeature(new DiscordBridgeFeature());
        registerFeature(new DiscordBlockFeature());

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
