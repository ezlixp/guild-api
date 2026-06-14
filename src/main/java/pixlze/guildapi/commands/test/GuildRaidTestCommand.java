package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.type.Prepend;

public class GuildRaidTestCommand extends ClientCommand {
    private static final Text THREE_PEOPLE_RAID_MESSAGE = Text.empty().setStyle(ColourUtils.AQUA)
            .append(Text.literal("\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE")
                    .setStyle(TextUtils.fontOf(Identifier.of("chat/prefix"))))
            .append(Text.literal(" "))
            .append(Text.literal("Pickelated").setStyle(ColourUtils.YELLOW))
            .append(Text.literal(", "))
            .append(Text.literal("vex710").setStyle(ColourUtils.YELLOW))
            .append(Text.literal(", and "))
            .append(Text.literal("Essentuan").setStyle(ColourUtils.YELLOW))
            .append(Text.literal(" finished "))
            .append(Text.literal("Orphion's").setStyle(ColourUtils.DARK_AQUA).append(Text.literal("\n")
                            .append(Text.empty().setStyle(ColourUtils.AQUA.withBold(false).withUnderline(false))
                                    .append(Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")
                                            .setStyle(TextUtils.fontOf(Identifier.of("chat/prefix"))))
                                    .append(Text.literal(" "))))
                    .append(Text.literal("Nexus of Light")))
            .append(Text.literal(" and claimed "))
            .append(Text.literal("2048x Emeralds").setStyle(ColourUtils.DARK_AQUA))
            .append(Text.literal(", "))
            .append(Text.literal("2x Aspects").setStyle(ColourUtils.DARK_AQUA))
            .append(Text.literal(", "))
            .append(Text.empty().setStyle(ColourUtils.DARK_AQUA).append(
                    Text.literal("\n")
                            .append(Text.empty().setStyle(ColourUtils.AQUA.withBold(false).withUnderline(false))
                                    .append(Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")
                                            .setStyle(TextUtils.fontOf(Identifier.of("chat/prefix"))))
                                    .append(Text.literal(" ")))
            ).append(Text.literal("+312m Guild Experience")))
            .append(Text.literal(", and "))
            .append(Text.literal("+330 Seasonal Rating").setStyle(ColourUtils.DARK_AQUA));

    public GuildRaidTestCommand() {
        super("raid");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            WynnChatMessage.EVENT.invoker().interact(THREE_PEOPLE_RAID_MESSAGE);
            McUtils.sendLocalMessage(THREE_PEOPLE_RAID_MESSAGE, Prepend.EMPTY.get(), false);
            return Command.SINGLE_SUCCESS;
        });
//        return base.then(
//                ClientCommandManager.argument("raid", StringArgumentType.word()).suggests((context, builder) -> {
//                            builder.suggest("notg");
//                            builder.suggest("nol");
//                            builder.suggest("tcc");
//                            builder.suggest("tna");
//                            return builder.buildFuture();
//                        })
//                        .then(ClientCommandManager.argument("player1", StringArgumentType.word())
//                                .then(ClientCommandManager.argument("player2", StringArgumentType.word())
//                                        .then(ClientCommandManager.argument("player3", StringArgumentType.word())
//                                                .then(ClientCommandManager.argument("player4", StringArgumentType.word())
//                                                        .executes((context) -> {
//                                                            String raid = StringArgumentType.getString(context, "raid");
//                                                            String player1 = StringArgumentType.getString(context, "player1");
//                                                            String player2 = StringArgumentType.getString(context, "player2");
//                                                            String player3 = StringArgumentType.getString(context, "player3");
//                                                            String player4 = StringArgumentType.getString(context, "player4");
//                                                            Text raidFinishedMessage = Text.literal("§b\uDAFF\uDFFC\uE001\uDB00\uDC06§b §e" + player1 + "§b, §e" + player2 + "§b, §e" + player3 + "§b, and §e" + player4 +
//                                                                    "§b finished §3" + raid + "thisisatestraid§b");
//                                                            WynnChatMessage.EVENT.invoker()
//                                                                    .interact(raidFinishedMessage);
//                                                            McUtils.sendLocalMessage(raidFinishedMessage, Text.empty(), false);
//                                                            return Command.SINGLE_SUCCESS;
//                                                        })
//                                                )
//                                        )
//                                )
//                        )
//        );
    }
}
