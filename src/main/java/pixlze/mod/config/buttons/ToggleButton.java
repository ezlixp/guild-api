package pixlze.mod.config.buttons;

import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import pixlze.mod.config.types.Toggle;

public class ToggleButton extends ToggleButtonWidget {
    private final Toggle toggle;

    public ToggleButton(Toggle toggle, int x, int y, int width, int height, ButtonTextures textures) {
        super(x, y, width, height, false);
        super.setTextures(textures);
        super.setToggled(toggle.getState());
        this.toggle = toggle;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.setToggled(!super.toggled);
        toggle.setState(super.toggled);
        System.out.println("togglers");
    }
}
