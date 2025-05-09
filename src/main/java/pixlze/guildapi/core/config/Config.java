package pixlze.guildapi.core.config;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ClassUtils;
import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.utils.McUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class Config<T> {
    private final Type type;
    private T value;
    private T pending;
    private String name;
    private Feature owner;
    private String i18nKey;

    public Config(T value) {
        this.value = value;
        this.type = new TypeToken<T>(getClass()) {
        }.getType();
    }

    public void setTranslationKey(String key) {
        i18nKey = key;
    }

    public T getValue() {
        return value;
    }

    public Type getTypeToken() {
        return type;
    }

    public Class<?> getType() {
        return value.getClass();
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
        return new TextWidget(Text.translatable(i18nKey + ".name"), McUtils.mc().textRenderer);
    }

    @SuppressWarnings("unchecked")
    public ClickableWidget getActionWidget() {
        if (getType().equals(Boolean.class)) {
            setPending(this.value);
            return ButtonWidget.builder(Text.of((boolean) this.pending ? "Yes":"No"), (button) -> {
                this.setPending((T) (this.pending.equals(Boolean.TRUE) ? Boolean.FALSE:Boolean.TRUE));
                button.setMessage(Text.of((boolean) this.pending ? "Yes":"No"));
            }).tooltip(Tooltip.of(Text.translatable(i18nKey + ".description"))).dimensions(0, 0, 80, 25 - 4).build();
        } else {
            TextFieldWidget out = new TextFieldWidget(McUtils.mc().textRenderer, 300, 25 - 4, Text.of("enter here"));
            out.setEditable(true);
            out.write(this.value.toString());
            out.setTooltip(Tooltip.of(Text.translatable(i18nKey + ".description")));
            out.setChangedListener((to) -> {
                tryParseStringValue(to).ifPresent(this::setPending);
            });
            out.setMaxLength(100);
            return out;
        }
    }

    @SuppressWarnings("unchecked")
    public Optional<T> tryParseStringValue(String value) {
        try {
            Class<?> wrapped = ClassUtils.primitiveToWrapper(getType());
            return Optional.of((T) wrapped.getConstructor(String.class).newInstance(value));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
