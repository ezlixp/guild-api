package pixlze.guildapi.screens.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import pixlze.guildapi.utils.McUtils;

public class AllowSectionSignTextField extends TextFieldWidget {
    private static final String SECTION_SIGN_PLACEHOLDER = "Ò";

    public AllowSectionSignTextField(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);
        this.addFormatter((message, idx) -> {
            return isFocused() ? OrderedText.styledForwardsVisitedString(message.replaceAll(SECTION_SIGN_PLACEHOLDER, "§"), Style.EMPTY):Text.literal(message).asOrderedText();
        });
    }

    public String getRealText() {
        return this.getText().replaceAll(SECTION_SIGN_PLACEHOLDER, "§");
    }

    @Override
    public void write(String text) {
        super.write(text.replaceAll("§", SECTION_SIGN_PLACEHOLDER));
        super.setText(super.getText().replace(SECTION_SIGN_PLACEHOLDER, "§"));
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!this.isActive()) {
            return false;

        } else if (input.isValidChar() || input.codepoint() == 167) {
            this.write(input.asString());

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setFocused(boolean focused) {
        if (focused && !this.isFocused()) {
            this.setText(this.getText().replaceAll("§", SECTION_SIGN_PLACEHOLDER));
            this.onClick(new Click(McUtils.mc().mouse.getX() / 2, McUtils.mc().mouse.getY() / 2, new MouseInput(0, 0)), false);
        } else if (!focused && this.isFocused())
            this.setText(this.getText().replaceAll(SECTION_SIGN_PLACEHOLDER, "§"));
        super.setFocused(focused);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
//        GuildApi.LOGGER.info("{} {}", McUtils.mc().textRenderer.getWidth("Ò"), McUtils.mc().textRenderer.getWidth("¶"));
        if (this.getText().contains("§§") || this.getText().contains(SECTION_SIGN_PLACEHOLDER + SECTION_SIGN_PLACEHOLDER)) {
            if (!isFocused())
                this.setTooltip(Tooltip.of(Text.literal("Warning ⚠\nThis regex contains a double format code. Click to edit.")));
            else this.setTooltip(null);
            context.fill(getX() - 1, getY() - 2, getX() + getWidth() + 2, getY() + getHeight() + 2, 0x55FF0000);
        } else {
            this.setTooltip(null);
        }
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }
}