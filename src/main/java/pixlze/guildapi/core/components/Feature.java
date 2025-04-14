package pixlze.guildapi.core.components;

import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.config.Configurable;

public abstract class Feature {
    @Configurable(i18nKey = "features." + GuildApi.MOD_ID + ".feature.featureEnabled")
    public final Config<Boolean> enabled = new Config<>(true);

    private final String name;

    protected Feature(String name) {
        this.name = name;
    }


    public abstract void init();

    public String getName() {
        return name;
    }

    public abstract void onConfigUpdate(Config<?> config);

    public void onEnabled() {
    }

    public void onDisabled() {
    }

    public boolean isEnabled() {
        return enabled.getValue();
    }

    private void toggle() {
        if (enabled.getValue()) {
            Managers.Feature.enableFeature(this);
        } else {
            Managers.Feature.disableFeature(this);
        }
    }

    public final void updateConfig(Config<?> config) {
        if (config.getName().equals("enabled")) {
            toggle();
            return;
        }
        onConfigUpdate(config);
    }
}
