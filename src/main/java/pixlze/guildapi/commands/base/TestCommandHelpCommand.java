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

class TestCommandHelpCommand extends ClientCommand {
    private static final List<Pair<String, String>> TEST_COMMANDS = List.of(
            new Pair<>("/setplayer <username>", "Impersonates specified username."),
            new Pair<>("/raid <notg|nol|tcc|tna> <player1> <player2> <player3> <player 4>", "Simulates raid completion."),
            new Pair<>("/tome <username>", "Simulates a tome given to specified username."),
            new Pair<>("/aspect <username>", "Simulates an aspect given to specified username."),
            new Pair<>("/testmessage <message>", "Simulates a guild message.")
    );

    private final MutableText helpMessage;

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            McUtils.sendLocalMessage(helpMessage, Prepend.DEFAULT.get(), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    public TestCommandHelpCommand() {
        super("testhelp");
        helpMessage = Text.literal("§aTest Commands:\n");
        for (int i = 0; i < TEST_COMMANDS.size(); i++) {
            Pair<String, String> entry = TEST_COMMANDS.get(i);
            String delimiter = entry.getLeft().isBlank() ? "":" - ";
            helpMessage.append(entry.getLeft() + delimiter + entry.getRight());
            if (i != TEST_COMMANDS.size() - 1)
                helpMessage.append("\n");
        }
    }
}
