package pixlze.guildapi.core.config;

import com.google.common.reflect.TypeToken;
import pixlze.guildapi.core.features.Feature;

import java.lang.reflect.Type;

public class Config<T> {
    private final Type type;
    private T value;
    private String name;
    private Feature owner;

    public Config(T value) {
        this.value = value;
        this.type = new TypeToken<T>(getClass()) {
        }.getType();
    }

    public T getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public void setValue(T value) {
        this.value = value;
        owner.updateConfig(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(Feature owner) {
        this.owner = owner;
    }
}
