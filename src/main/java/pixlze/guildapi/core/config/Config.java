package pixlze.guildapi.core.config;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ClassUtils;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.utils.McUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class Config<T> {
    private final Type type;
    private T value;
    private T pending;
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

    public void setPending(T value) {
        this.pending = value;
    }

    public void applyPending() {
        if (pending != null && !pending.equals(value)) {
            this.value = pending;
            pending = null;
            owner.updateConfig(this);
        }
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

    public TextWidget getTitleWidget() {
        return new TextWidget(300, 25, Text.of(getName()), McUtils.mc().textRenderer);
    }

    public ClickableWidget getActionWidget() {
        TextFieldWidget out = new TextFieldWidget(McUtils.mc().textRenderer, 300, 25, Text.of("enter here"));
        out.setEditable(true);
        out.write(this.value.toString());
        out.setChangedListener((to) -> {
            tryParseStringValue(to).ifPresent(this::setPending);
        });
        return out;
    }

    @SuppressWarnings("unchecked")
    public Optional<T> tryParseStringValue(String value) {
        try {
            Class<?> wrapped = ClassUtils.primitiveToWrapper(this.value.getClass());
            return Optional.of((T) wrapped.getConstructor(String.class).newInstance(value));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
