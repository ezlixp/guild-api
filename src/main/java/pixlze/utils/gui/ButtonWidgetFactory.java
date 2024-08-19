package pixlze.utils.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ButtonWidgetFactory {
    static public ButtonWidget build(Text message, ButtonWidget.PressAction onPress, int x, int y, int width, int height) {
        ButtonWidget.Builder buttonBuilder = new ButtonWidget.Builder(message, onPress);
        buttonBuilder.dimensions(x, y, width, height);
        return buttonBuilder.build();
    }
}
