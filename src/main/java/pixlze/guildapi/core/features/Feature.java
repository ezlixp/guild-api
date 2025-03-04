package pixlze.guildapi.core.features;

import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.config.Configurable;

public abstract class Feature {
    @Configurable
    private Config<Boolean> enabled = new Config<>(true);

    public abstract void init();

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
