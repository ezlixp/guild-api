package pixlze.guildapi.screens.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;

public class FeatureConfigListWidget extends ElementListWidget<FeatureConfigWidget> {
    public FeatureConfigListWidget(MinecraftClient client, ConfigScreen configScreen, int width) {
        super(client, width, configScreen.layout.getContentHeight(), configScreen.layout.getHeaderHeight(), 25);
        // 25 row length, override get item count to make each feature config widget count for each of its configs

    }

    @Override
    protected int getEntryCount() {
        int out = 0;
        for (FeatureConfigWidget child : children()) {
            out += child.children().size() + 2; // extra 2 for title and one row of spacing
        }
        return out;
    }
}
