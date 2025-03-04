package pixlze.guildapi.core.config;

import pixlze.guildapi.core.Manager;
import pixlze.guildapi.core.features.Feature;

import java.util.HashMap;

public class ConfigManager extends Manager {
    private HashMap<? extends Feature, Config<?>> configs;

    public void init() {
        // register all features with config, load current config file
    }

    public synchronized void saveConfig() {

    }
}
