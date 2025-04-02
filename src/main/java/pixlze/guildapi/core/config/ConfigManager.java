package pixlze.guildapi.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Manager;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.utils.JsonUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager extends Manager {
    private static final File CONFIG_DIR = GuildApi.getModStorageDir("config");
    private final LinkedHashMap<Feature, List<Config<?>>> configs = new LinkedHashMap<>();

    private final File configFile;
    private JsonObject configObject;

    public ConfigManager() {
        configFile = new File(CONFIG_DIR, "config.json");
    }

    public void init() {
        configObject = Managers.Json.loadJsonFromFile(configFile);
        Managers.Feature.getFeatures().forEach(this::registerFeature);
        saveConfig();
    }

    public synchronized void saveConfig() {
        configObject = new JsonObject();

        for (Map.Entry<Feature, List<Config<?>>> entry : configs.entrySet()) {
            JsonObject curConfig = new JsonObject();
            for (Config<?> config : entry.getValue()) {
                config.applyPending();
                curConfig.add(config.getName(), JsonUtils.toJsonElement(config.getValue().toString()));
            }
            configObject.add(entry.getKey().getClass().getSimpleName(), curConfig);
        }

        if (!Managers.Json.saveJsonAsFile(configFile, configObject)) {
            GuildApi.LOGGER.warn("couldn't save config");
        }
    }

    private void registerFeature(Feature feature) {
        List<Config<?>> featureConfigs = new ArrayList<>();
        JsonObject featureConfigObject = new JsonObject();
        JsonElement temp = configObject.get(feature.getClass().getSimpleName());
        if (temp != null)
            featureConfigObject = temp.getAsJsonObject();

        for (Field field : feature.getClass().getFields()) {
            if (!field.isAnnotationPresent(Configurable.class)) continue;
            try {
                Config<?> config = (Config<?>) field.get(feature);
                config.setName(field.getName());
                config.setOwner(feature);

                if (featureConfigObject.get(config.getName()) != null) {
                    config.setPending(Managers.Json.GSON.fromJson(featureConfigObject.get(config.getName()), config.getType()));
                }

                featureConfigs.add(config);
            } catch (Exception e) {
                GuildApi.LOGGER.error("config register error: {} {}", e, e.getMessage());
            }
        }
        configs.put(feature, featureConfigs);
    }

    public List<Config<?>> getFeatureConfigs(Feature feature) {
        return configs.get(feature);
    }
}
