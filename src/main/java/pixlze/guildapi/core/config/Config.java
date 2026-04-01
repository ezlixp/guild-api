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
    private boolean syncOnline = false;
    private boolean disabled = false;
    private String syncUri;
    private int cycleLength;

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

    private void setValue(T value) {
        this.value = value;
    }

    public void setPending(T value) {
        this.pending = value;
    }

    public boolean getSyncOnline() {
        return syncOnline;
    }

    public void setSyncOnline(boolean value) {
        this.syncOnline = value;
    }

    public String getSyncUri() {
        return syncUri;
    }

    public void setSyncUri(String value) {
        this.syncUri = value;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean dis) {
        this.disabled = dis;
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(int value) {
        this.cycleLength = value;
    }

    public void applyPending() {
        if (pending != null && !pending.equals(value)) {
            setValue(pending);
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
        ClickableWidget out;
        if (getType().equals(Boolean.class)) {
            setPending(this.value);

            out = ButtonWidget.builder(Text.of((boolean) this.pending ? "Yes":"No"), (button) -> {
                this.setPending((T) (this.pending.equals(Boolean.TRUE) ? Boolean.FALSE:Boolean.TRUE));
                button.setMessage(Text.of((boolean) this.pending ? "Yes":"No"));
            }).tooltip(Tooltip.of(Text.translatable(i18nKey + ".description"))).dimensions(0, 0, 100, 25 - 4).build();
        } else if (Number.class.isAssignableFrom(getType()) && cycleLength > 0) {
            if (this.value.getClass() == Double.class)
                this.value = (T) Integer.valueOf(((Double) this.value).intValue());

            setPending(this.value);

            out = ButtonWidget.builder(Text.translatable(i18nKey + ".display." + this.pending), (button) -> {
                this.setPending((T) Integer.valueOf((((int) this.pending + 1) % this.cycleLength)));
                button.setMessage(Text.translatable(i18nKey + ".display." + this.pending));
                button.setTooltip(Tooltip.of(Text.translatable(i18nKey + ".description." + this.pending)));
            }).tooltip(Tooltip.of(Text.translatable(i18nKey + ".description." + this.pending))).dimensions(0, 0, 100, 25 - 4).build();
        } else {
            TextFieldWidget temp = new TextFieldWidget(McUtils.mc().textRenderer, 100, 25 - 4, Text.of("enter here"));
            temp.setEditable(true);
            temp.write(this.value.toString());
            temp.setTooltip(Tooltip.of(Text.translatable(i18nKey + ".description")));
            temp.setChangedListener((to) -> {
                tryParseStringValue(to).ifPresent(this::setPending);
            });
            temp.setMaxLength(100);
            out = temp;
        }
        if (isDisabled()) {
            // active disables all interaction with it by stopping the mouse click event
            out.active = false;
            out.setTooltip(Tooltip.of(Text.literal("Please connect to the guild server to activate this option.")));
        }
        return out;
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
