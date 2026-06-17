package pixlze.guildapi.core.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractConfigEditScreen<T> extends Screen {
    protected final Screen parent;
    protected final Config<T> myConfig;

    protected AbstractConfigEditScreen(String title, Screen parent, Config<T> myConfig) {
        super(Text.literal(title));
        this.parent = parent;
        this.myConfig = myConfig;
    }

    public AbstractConfigEditScreen(Screen parent, Config<T> myConfig) {
        super(Text.literal("fallback"));
        this.parent = parent;
        this.myConfig = myConfig;
    }

    abstract protected void fillConfig();
    
    @Override
    public void removed() {
        fillConfig();
    }

}
