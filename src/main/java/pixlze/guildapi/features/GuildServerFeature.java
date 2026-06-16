package pixlze.guildapi.features;

import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.Config;

public class GuildServerFeature extends Feature {
    public GuildServerFeature() {
        super("Guild Server");
    }

    @Override
    public void init() {

    }

    @Override
    public void onConfigUpdate(Config<?> config) {
        if (config.getName().equals("enabled")) {
            Managers.Config.setDisabledSocketFeatureConfigs((boolean) config.getValue());
        }
    }
}
