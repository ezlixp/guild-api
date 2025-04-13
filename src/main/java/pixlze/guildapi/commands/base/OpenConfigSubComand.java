package pixlze.guildapi.commands.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.screens.config.ConfigScreen;
import pixlze.guildapi.utils.McUtils;

public class OpenConfigSubComand extends ClientCommand {
    public OpenConfigSubComand() {
        super("config");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((source) -> {
            Managers.Tick.scheduleNextTick(() -> McUtils.mc().setScreen(new ConfigScreen(McUtils.mc().currentScreen)));
            return Command.SINGLE_SUCCESS;
        });
    }
}
