package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.McUtils;

public class GuildRaidTestCommand extends ClientCommand {
    public GuildRaidTestCommand() {
        super("raid");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(
                ClientCommandManager.argument("raid", StringArgumentType.word()).suggests((context, builder) -> {
                            builder.suggest("notg");
                            builder.suggest("nol");
                            builder.suggest("tcc");
                            builder.suggest("tna");
                            return builder.buildFuture();
                        })
                        .then(ClientCommandManager.argument("player1", StringArgumentType.word())
                                .then(ClientCommandManager.argument("player2", StringArgumentType.word())
                                        .then(ClientCommandManager.argument("player3", StringArgumentType.word())
                                                .then(ClientCommandManager.argument("player4", StringArgumentType.word())
                                                        .executes((context) -> {
                                                            String raid = StringArgumentType.getString(context, "raid");
                                                            String player1 = StringArgumentType.getString(context, "player1");
                                                            String player2 = StringArgumentType.getString(context, "player2");
                                                            String player3 = StringArgumentType.getString(context, "player3");
                                                            String player4 = StringArgumentType.getString(context, "player4");
                                                            Text raidFinishedMessage = Text.literal("§b\uDAFF\uDFFC\uE001\uDB00\uDC06§b §e" + player1 + "§b, §e" + player2 + "§b, §e" + player3 + "§b, and §e" + player4 +
                                                                    "§b finished §3" + raid + "thisisatestraid§b");
                                                            WynnChatMessage.EVENT.invoker().interact(raidFinishedMessage);
                                                            McUtils.sendLocalMessage(raidFinishedMessage, Text.empty(), false);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
        );
    }
}
