package pixlze.guildapi.core.config;

import pixlze.guildapi.core.Managers;

public class Config<T> {
    private T value;
    private String name;

    public Config(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        touched();
    }

    public void touched() {
        Managers.Config.saveConfig();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
