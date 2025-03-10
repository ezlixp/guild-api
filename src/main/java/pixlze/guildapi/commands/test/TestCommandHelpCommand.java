package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;

public class TestCommandHelpCommand extends ClientCommand {
    private static final List<Pair<String, String>> TEST_COMMANDS = List.of(
            new Pair<>("/setplayer <username>", "Impersonates specified username."),
            new Pair<>("/raid <notg|nol|tcc|tna> <player1> <player2> <player3> <player 4>", "Simulates raid completion."),
            new Pair<>("/tome <username>", "Simulates a tome given to specified username."),
            new Pair<>("/aspect <username>", "Simulates an aspect given to specified username."),
            new Pair<>("/testmessage <message>", "Simulates a guild message.")
    );

    private final MutableText helpMessage;

    public TestCommandHelpCommand() {
        super("TestCommandHelp");
        helpMessage = Text.literal("§aTest Commands:\n");
        for (int i = 0; i < TEST_COMMANDS.size(); i++) {
            Pair<String, String> entry = TEST_COMMANDS.get(i);
            String delimiter = entry.getLeft().isBlank() ? "":" - ";
            helpMessage.append(entry.getLeft() + delimiter + entry.getRight());
            if (i != TEST_COMMANDS.size() - 1)
                helpMessage.append("\n");
        }
        setCommand(GuildApi.BASE_COMMAND.then(ClientCommandManager.literal("testhelp").executes((context) -> {
            McUtils.sendLocalMessage(helpMessage, Prepend.DEFAULT.get(), false);
            return Command.SINGLE_SUCCESS;
        })));
    }
}
