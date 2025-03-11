package pixlze.guildapi.commands.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;

class ClientCommandHelpCommand extends ClientCommand {
    private static final List<Pair<String, String>> COMMANDS = List.of(
            new Pair<>("/guildapi help", "Displays this list of commands."),

            new Pair<>("\n", "Bridge:"),

            new Pair<>("/discord <message>", "Sends a guild chat message that is only visible to other mod users and the discord."),
            new Pair<>("/reconnect", "Tries to connect to the chat server if it isn't already connected."),
            new Pair<>("/online", "Displays all connected mod users."),

            new Pair<>("\n", "Block List:"),

            new Pair<>("/blocklist", "Lists all blocked usernames."),
            new Pair<>("/blocklist add <username>", "Blocks all discord type messages from a specified discord/minecraft username."),
            new Pair<>("/blocklist remove <username>", "Unblocks a specified discord/minecraft username."),

            new Pair<>("\n", "Tome List:"),

            new Pair<>("/tomelist", "Displays the current queue to get a guild tome."),
            new Pair<>("/tomelist add", "Adds you to the tome list queue if you're not already listed."),
            new Pair<>("/tomelist search <player>", "Fetches the position of a specified player in the tome list queue, or your position if no player is specified."),

            new Pair<>("\n", "Raid List:"),

            new Pair<>("/raidlist", "Displays raid information for all players."),
            new Pair<>("/raidlist search <player>", "Fetches the raid information for a specific player, or your information if no player is specified."),
            new Pair<>("/raidlist sort <raids|apsects|emeralds>", "Defines how to sort information displayed in the raid list.")
    );

    private final MutableText helpMessage;

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            McUtils.sendLocalMessage(helpMessage, Prepend.DEFAULT.get(), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    public ClientCommandHelpCommand() {
        super("help");
        helpMessage = Text.literal("§aCommands:\n");
        for (int i = 0; i < COMMANDS.size(); i++) {
            Pair<String, String> entry = COMMANDS.get(i);
            String delimiter = entry.getLeft().isBlank() ? "":" - ";
            helpMessage.append(entry.getLeft() + delimiter + entry.getRight());
            if (i != COMMANDS.size() - 1)
                helpMessage.append("\n");
        }
    }
}
