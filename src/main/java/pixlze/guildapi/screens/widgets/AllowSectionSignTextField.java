package pixlze.guildapi.screens.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;

public class AllowSectionSignTextField extends TextFieldWidget {
    public AllowSectionSignTextField(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);
    }

    @Override
    public void write(String text) {
        super.write(text.replaceAll("§", "\uD83D\uDE00"));
        super.setText(super.getText().replace("\uD83D\uDE00", "§"));
    }

    @Override
    public boolean charTyped(CharInput input) {
        GuildApi.LOGGER.info("{}", input.codepoint());
        if (!this.isActive()) {
            return false;

        } else if (input.isValidChar() || input.codepoint() == 167) {
            this.write(input.asString());

            return true;
        } else {
            return false;
        }
    }
}