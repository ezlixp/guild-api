package pixlze.guildapi.features.list;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ListSubCommand {
    public String name;
    Command<FabricClientCommandSource> runs;

    public ListSubCommand(String name, Command<FabricClientCommandSource> runs) {
        this.name = name;
        this.runs = runs;
    }
}
