package pixlze.guildapi.commands.base.screen;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.screens.menu.MenuScreen;
import pixlze.guildapi.utils.McUtils;

public class OpenMenuSubComand extends ClientCommand {
    public OpenMenuSubComand() {
        super("menu");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((source) -> {
            Managers.Tick.scheduleNextTick(() -> McUtils.mc().setScreen(new MenuScreen(McUtils.mc().currentScreen)));
            return Command.SINGLE_SUCCESS;
        });
    }
}
