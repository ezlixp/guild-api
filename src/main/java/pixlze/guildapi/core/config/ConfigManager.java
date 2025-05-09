package pixlze.guildapi.core.config;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.core.components.Managers;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class ConfigManager extends Manager {
    private static final File CONFIG_DIR = GuildApi.getModStorageDir("config");
    private final LinkedHashMap<Feature, List<Config<?>>> configs = new LinkedHashMap<>();

    private final File configFile;
    private JsonObject configObject;

    public ConfigManager() {
        super(List.of());
        configFile = new File(CONFIG_DIR, "config.json");
    }

    public void init() {
        configObject = Managers.Json.loadJsonFromFile(configFile);
        Managers.Feature.getFeatures().forEach(this::registerFeature);
        saveConfig();
    }

    public JsonObject getConfigObject() {
        return configObject;
    }

    public synchronized void saveConfig() {
        configObject = new JsonObject();

        for (Map.Entry<Feature, List<Config<?>>> entry : configs.entrySet()) {
            JsonObject curConfig = new JsonObject();
            for (Config<?> config : entry.getValue()) {
                config.applyPending();
                if (config.getValue().getClass() == String.class)
                    curConfig.addProperty(config.getName(), config.getValue().toString());
                else
                    curConfig.add(config.getName(), Managers.Json.toJsonElement(config.getValue().toString()));
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
                if (!field.getAnnotation(Configurable.class).i18nKey().isBlank())
                    config.setTranslationKey(field.getAnnotation(Configurable.class).i18nKey());
                else {
                    config.setTranslationKey("feature." + GuildApi.MOD_ID + "." + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, feature.getClass().getSimpleName().replace("Feature", "")) + "." + field.getName());
                }
                config.setOwner(feature);

                if (featureConfigObject.get(config.getName()) != null) {
                    Object toSet = Managers.Json.GSON.fromJson(featureConfigObject.get(config.getName()), config.getTypeToken());
                    if (toSet.getClass() == config.getValue().getClass())
                        config.setPending(Managers.Json.GSON.fromJson(featureConfigObject.get(config.getName()), config.getTypeToken()));
                }

                if (Objects.equals(config.getName(), "enabled")) featureConfigs.addFirst(config);
                else featureConfigs.add(config);
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
