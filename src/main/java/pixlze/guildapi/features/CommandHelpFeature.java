package pixlze.guildapi.features;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;

public class CommandHelpFeature extends Feature {
    private final List<Pair<String, String>> commands = List.of(
            new Pair<>("/guildapi (/gapi) help", "Displays this list of commands."),

            new Pair<>("\n", "Bridge:"),

            new Pair<>("/discord (/dc) <message>", "Sends a guild chat message that is only visible to other mod users and the discord."),
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

    private MutableText helpMessage;

    @Override
    public void init() {
        helpMessage = Text.literal("§aCommands:\n");
        for (int i = 0; i < commands.size(); i++) {
            Pair<String, String> entry = commands.get(i);
            String delimiter = entry.getLeft().isBlank() ? "":" - ";
            helpMessage.append(entry.getLeft() + delimiter + entry.getRight());
            if (i != commands.size() - 1)
                helpMessage.append("\n");
        }
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(GuildApi.BASE_COMMAND.then(ClientCommandManager.literal("help").executes((context) -> {
                McUtils.sendLocalMessage(helpMessage, Prepend.DEFAULT.get(), false);
                return Command.SINGLE_SUCCESS;
            })));
        }));
    }
}
