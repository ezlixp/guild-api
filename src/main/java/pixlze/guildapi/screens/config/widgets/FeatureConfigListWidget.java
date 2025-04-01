package pixlze.guildapi.screens.config.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.screens.widgets.DynamicSizeElementListWidget;

import java.util.ArrayList;
import java.util.List;

public class FeatureConfigListWidget extends DynamicSizeElementListWidget<FeatureConfigListWidget.Entry> {

    // if ever need subconfigs, create new type of widget that has section header as one pair of widget, the other piece is the dynamic size element list widget
    // then that second class can be reused to hold subconfigs
    // Config<List<Config>>
    public FeatureConfigListWidget() {
        super(Text.empty());
        for (Feature feature : Managers.Feature.getFeatures()) {
            this.add(new Entry(feature));
        }
    }

    public static class Entry extends DynamicSizeElementListWidget.Entry<FeatureConfigListWidget.Entry> implements ParentElement {
        private final List<ClickableWidget> widgets = new ArrayList<>();
        private final Feature feature;

        public Entry(Feature feature) {
            this.feature = feature;
        }

        public int getHeight() {
            return 1000;
        }

        @Override
        public void render(DrawContext context, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, float tickDelta) {
            int pad = 0;
            for (ClickableWidget child : widgets) {
                child.setPosition(x, y + pad);
                child.render(context, mouseX, mouseY, tickDelta);
                pad += child.getHeight() + 4;
            }
            context.fill(x, y, x + entryWidth, y + entryHeight, 0x8000FF00 + index * 10);
        }

        @Override
        public List<ClickableWidget> children() {
            return widgets;
        }

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public void setDragging(boolean dragging) {

        }

        @Override
        public @Nullable Element getFocused() {
            return null;
        }

        @Override
        public void setFocused(@Nullable Element focused) {

        }
    }
}
