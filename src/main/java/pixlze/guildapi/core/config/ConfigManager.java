package pixlze.guildapi.core.config;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.McUtils;

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
        try {
            configObject = Managers.Json.loadJsonFromFile(configFile).getAsJsonObject();
        } catch (Exception e) {
            configObject = new JsonObject();
            GuildApi.LOGGER.warn("config load error: {} {}", e, e.getMessage());
        }
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
                if (config.getSyncOnline()) {
                    String syncUri = config.getSyncUri() + McUtils.playerUUID();
                    if (!Managers.Net.guild.isDisabled())
                        Managers.Net.guild.post(syncUri, Managers.Json.toJsonObject("{" + config.getName() + ":" + config.getValue().toString() + "}"), false);
                } else {
                    if (config.getValue().getClass() == String.class)
                        curConfig.addProperty(config.getName(), config.getValue().toString());
                    else
                        curConfig.add(config.getName(), Managers.Json.toJsonElement(config.getValue().toString()));
                }
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
            if (!field.isAnnotationPresent(Configurable.class) && !field.isAnnotationPresent(SyncConfigurable.class))
                continue;
            try {
                Config<?> config = (Config<?>) field.get(feature);
                config.setName(field.getName());
                config.setOwner(feature);
                String simpleFeatureName = feature.getClass().getSimpleName().replace("Feature", "");
                if (field.isAnnotationPresent(Configurable.class)) {
                    if (!field.getAnnotation(Configurable.class).i18nKey().isBlank())
                        config.setTranslationKey(field.getAnnotation(Configurable.class).i18nKey());
                    else {
                        config.setTranslationKey("feature." + GuildApi.MOD_ID + "." + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, simpleFeatureName) + "." + field.getName());
                    }

                    if (featureConfigObject.get(config.getName()) != null) {
                        Object toSet = Managers.Json.GSON.fromJson(featureConfigObject.get(config.getName()), config.getTypeToken());
                        if (toSet.getClass() == config.getValue().getClass())
                            config.setPending(Managers.Json.GSON.fromJson(featureConfigObject.get(config.getName()), config.getTypeToken()));
                    }

                    if (Objects.equals(config.getName(), "enabled")) featureConfigs.addFirst(config);
                    else featureConfigs.add(config);
                } else if (field.isAnnotationPresent(SyncConfigurable.class)) {
                    config.setSyncOnline(true);
                    config.setSyncUri(field.getAnnotation(SyncConfigurable.class).syncUri());
                    if (!field.getAnnotation(SyncConfigurable.class).i18nKey().isBlank())
                        config.setTranslationKey(field.getAnnotation(SyncConfigurable.class).i18nKey());
                    else {
                        config.setTranslationKey("feature." + GuildApi.MOD_ID + "." + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, simpleFeatureName) + "." + config.getName());
                    }

                    config.setCycleLength(field.getAnnotation(SyncConfigurable.class).cycleLength());

                    if (!Managers.Net.guild.isDisabled()) {
                        String syncUri = config.getSyncUri() + McUtils.playerUUID();
                        com.google.gson.JsonElement resBody = Managers.Json.toJsonElement(Managers.Net.guild.get(syncUri, false).get().body());
                        Object toSet = Managers.Json.GSON.fromJson(resBody, config.getValue().getClass());
                        if (toSet.getClass() == config.getValue().getClass()) {
                            config.setPending(Managers.Json.GSON.fromJson(resBody, config.getTypeToken()));
                        }
                    }

                    // to prevent duplicates, only add to feature configs list if the current field isn't also annotated with configurable
                    if (!field.isAnnotationPresent(Configurable.class)) {
                        if (Objects.equals(config.getName(), "enabled")) featureConfigs.addFirst(config);
                        else featureConfigs.add(config);
                    }

                }
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
