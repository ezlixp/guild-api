package pixlze.mod.config;

import pixlze.mod.config.types.Option;
import pixlze.mod.config.types.Toggle;

import java.util.ArrayList;

public class PixUtilsConfig {
    private static final ArrayList<Option> options = new ArrayList<>();

    public static Toggle registerToggle(String name) {
        Toggle registeredToggle = new Toggle(name);
        options.add(registeredToggle);
        return registeredToggle;
    }

    public static Toggle registerToggle(String name, ArrayList<Option> children) {
        Toggle registeredToggle = new Toggle(name, children);
        options.add(registeredToggle);
        return registeredToggle;
    }

    public static ArrayList<Option> getOptions() {
        return new ArrayList<>(options);
    }
}
