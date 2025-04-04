package pixlze.guildapi.screens.config.widgets;

import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import pixlze.guildapi.core.config.Config;

import java.util.List;

public class ConfigRow extends AbstractParentElement {
    private final TextWidget title;
    private final ClickableWidget action;
    private int y;
    private int height;
    private int x;
    private int width;

    public ConfigRow(Config<?> config) {
        title = config.getTitleWidget();
        action = config.getActionWidget();
    }

    @Override
    public List<ClickableWidget> children() {
        return List.of(title, action);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return x <= mouseX && mouseX <= x + width && y <= mouseY && mouseY <= y + height;
    }

    protected void render(DrawContext context, int mouseX, int mouseY, float delta, int x, int y, int rowWidth, int rowHeight) {
        this.x = x;
        this.y = y;
        this.width = rowWidth;
        this.height = rowHeight;
        title.setPosition(x + 4, y);
        action.setPosition(x + rowWidth - action.getWidth() - 4, y);
        title.render(context, mouseX, mouseY, delta);
        action.render(context, mouseX, mouseY, delta);
        title.setHeight(rowHeight - 4);
        action.setHeight(rowHeight - 4);
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused)
            this.setFocused(null);
        super.setFocused(focused);
    }
}
