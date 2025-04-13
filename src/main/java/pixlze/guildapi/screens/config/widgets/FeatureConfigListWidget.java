package pixlze.guildapi.screens.config.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.screens.config.ConfigScreen;
import pixlze.guildapi.screens.widgets.DynamicSizeElementListWidget;
import pixlze.guildapi.utils.McUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeatureConfigListWidget extends DynamicSizeElementListWidget<FeatureConfigListWidget.Entry> {
    private final ConfigScreen configScreen;

    // if ever need subconfigs, create new type of widget that has section header as one pair of widget, the other piece is the dynamic size element list widget
    // then that second class can be reused to hold subconfigs
    // Config<List<Config>>
    public FeatureConfigListWidget(ConfigScreen configScreen, int width) {
        super(McUtils.mc(), width, configScreen.layout.getContentHeight(), configScreen.layout.getHeaderHeight());
        this.configScreen = configScreen;
        for (Feature feature : Managers.Feature.getFeatures())
            this.addEntry(createEntry(feature));
    }

    private Entry createEntry(Feature feature) {
        return new Entry(feature, new TextWidget(configScreen.width, 25, Text.of(feature.getName()), McUtils.mc().textRenderer));
    }


    public static class Entry extends DynamicSizeElementListWidget.Entry<FeatureConfigListWidget.Entry> implements ParentElement {
        private final List<ConfigRow> rows = new ArrayList<>();
        private final int rowHeight = 25;
        private final int headerHeight;
        private final ClickableWidget headerWidget;
        private Element focused;

        public Entry(Feature feature, ClickableWidget headerWidget) {
            this.headerWidget = headerWidget;
            this.headerHeight = headerWidget.getHeight();
            for (Config<?> config : Managers.Config.getFeatureConfigs(feature)) {
                rows.add(new ConfigRow(config));
            }
        }

        public int rowCount() {
            return children().size();
        }

        public int getHeight() {
            return rowHeight * rowCount() + headerHeight + 4;
        }

        @Override
        public void render(DrawContext context, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, float tickDelta) {
            context.fill(x, y, x + entryWidth, y + entryHeight, 0x8000FF00 + index * 100);
            headerWidget.setPosition(x, y);
            headerWidget.setWidth(entryWidth);
            headerWidget.render(context, mouseX, mouseY, tickDelta);
            int top = y + headerHeight + 4;
            for (ConfigRow child : rows) {
                child.render(context, mouseX, mouseY, tickDelta, x, top, entryWidth, rowHeight);
                top += rowHeight;
            }
        }

        @Override
        public List<ConfigRow> children() {
            return rows;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Optional<Element> optional = this.hoveredElement(mouseX, mouseY);
            if (optional.isPresent()) {
                if (optional.get().mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(optional.get());
                    if (button == 0) {
                        this.setDragging(true);
                    }
                }
            }
            return optional.isPresent();
        }

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public void setDragging(boolean dragging) {

        }

        @Nullable
        @Override
        public Element getFocused() {
            return this.focused;
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            if (this.focused != null && !this.focused.equals(focused)) {
                this.focused.setFocused(false);
            }

            if (focused != null) {
                focused.setFocused(true);
            }

            this.focused = focused;
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused) this.setFocused(null);
            ParentElement.super.setFocused(false);
        }
    }
}
