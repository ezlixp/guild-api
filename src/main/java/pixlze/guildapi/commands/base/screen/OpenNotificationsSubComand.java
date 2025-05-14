package pixlze.guildapi.commands.base.screen;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.screens.notifications.NotificationsEditScreen;
import pixlze.guildapi.utils.McUtils;

import java.util.List;

public class OpenNotificationsSubComand extends ClientCommand {
    public OpenNotificationsSubComand() {
        super("notifications");
    }

    @Override
    public List<String> getAliases() {
        return List.of("notifs");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((source) -> {
            Managers.Tick.scheduleNextTick(() -> McUtils.mc().setScreen(new NotificationsEditScreen(McUtils.mc().currentScreen)));
            return Command.SINGLE_SUCCESS;
        });
    }
}
