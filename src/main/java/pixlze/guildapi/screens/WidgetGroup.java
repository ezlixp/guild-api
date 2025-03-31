package pixlze.guildapi.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class WidgetGroup extends ContainerWidget {
    private final List<ClickableWidget> children = new ArrayList<>();
    private int contentsHeight = 0;

    public WidgetGroup(Text message) {
        super(0, 0, 0, 0, message);
    }

    /**
     * Adds a child widget to this widget group.
     * The x and y values of child are now the x and y padding values from the left wall
     * and the previous widget respectively.
     */
    protected <T extends ClickableWidget> void add(T child) {
        children.add(child);
        contentsHeight += child.getHeight();
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return contentsHeight;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 7;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ClickableWidget child : children())
            child.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public List<ClickableWidget> children() {
        return children;
    }
}
