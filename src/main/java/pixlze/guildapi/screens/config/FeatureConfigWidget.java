package pixlze.guildapi.screens.config;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.utils.McUtils;

import java.util.ArrayList;
import java.util.List;

public class FeatureConfigWidget extends AlwaysSelectedEntryListWidget.Entry<FeatureConfigWidget> {
    private final Feature feature;
    private final TextRenderer textRenderer;
    private final List<Widget> children = new ArrayList<>();

    public FeatureConfigWidget(Feature feature) {
        this.feature = feature;
        this.textRenderer = McUtils.mc().textRenderer;
    }

    public void init(int x, int y, int width) {
        this.add(new TextWidget(0, 0, textRenderer.getWidth(feature.getName()), textRenderer.fontHeight, Text.of(feature.getName()), textRenderer));
    }

    @Override
    public List<ClickableWidget> selectableChildren() {
        return children;
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

    }

    @Override
    public Text getNarration() {
        return null;
    }
}
