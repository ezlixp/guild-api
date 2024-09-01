package pixlze.pixutils.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import pixlze.pixutils.screens.config.PixUtilsConfigScreen;

public class PixUtilsModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PixUtilsConfigScreen::new;
    }
}
