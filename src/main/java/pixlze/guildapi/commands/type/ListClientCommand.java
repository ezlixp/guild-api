package pixlze.guildapi.commands.type;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ListClientCommand extends ClientCommand {
    protected String endpoint;
    private final String name;
    private final BiFunction<JsonElement, String, MutableText> lineParser;
    private List<JsonElement> cachedResponse;
    private String sortMember;
    private String extra;

    public ListClientCommand(String name, String endpoint, Function<JsonElement, MutableText> lineParser) {
        super(name + "list");
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = (listItem, sortBy) -> lineParser.apply(listItem);
    }

    public ListClientCommand(String name, String endpoint, Function<JsonElement, MutableText> lineParser, String sortMember) {
        super(name + "list");
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = (listItem, sortBy) -> lineParser.apply(listItem);
        this.sortMember = sortMember;
    }

    public ListClientCommand(String name, String endpoint, BiFunction<JsonElement, String, MutableText> lineParser) {
        super(name + "list");
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = lineParser;
    }

    public ListClientCommand(String name, String endpoint, BiFunction<JsonElement, String, MutableText> lineParser, String sortMember) {
        super(name + "list");
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = lineParser;
        this.sortMember = sortMember;
    }

    protected void setExtra(String extra) {
        this.extra = extra;
    }

    private String getExtra() {
        return extra != null ? extra:Managers.Net.guild.guildId;
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = base.executes(context -> {
            listItems(0, true);
            return Command.SINGLE_SUCCESS;
        }).then(ClientCommandManager.literal("view")
                .then(ClientCommandManager.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int page = IntegerArgumentType.getInteger(context, "page");
                            listItems(page - 1, true);
                            return Command.SINGLE_SUCCESS;
                        }).then(ClientCommandManager.argument("reload", BoolArgumentType.bool())
                                .executes(context -> {
                                    int page = IntegerArgumentType.getInteger(context, "page");
                                    boolean reload = BoolArgumentType.getBool(context, "reload");
                                    listItems(page - 1, reload);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )).executes(context -> {
                    syntaxError();
                    return Command.SINGLE_SUCCESS;
                }));
        for (ClientCommand subCommand : getSubCommands())
            for (LiteralArgumentBuilder<FabricClientCommandSource> command : subCommand.getCommands())
                builder.then(command);
        return builder;
    }

    protected String getSortMember() {
        return this.sortMember;
    }

    protected void setSortMember(String sortMember) {
        this.sortMember = sortMember;
    }

    private void listItems(int page, boolean reload) {
        CompletableFuture<List<JsonElement>> response;
        if (reload) {
            response = Managers.Net.guild.getList(endpoint + getExtra(), false, sortMember);
        } else {
            response = new CompletableFuture<>();
            response.complete(cachedResponse);
        }
        response.whenCompleteAsync((res, exception) -> {
            if (exception != null) {
                McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                GuildApi.LOGGER.error("List feature error: {} {}", exception, exception.getMessage());
                return;
            }
            cachedResponse = res;
            if (res == null) {
                assert Formatting.YELLOW.getColorValue() != null;
                // if not reload and we have res == null that means that we have no cached data and didn't fetch any data
                // if reload is true, res should never be null if it completed non exceptionally because it'll return empty array if no data
                if (!reload)
                    McUtils.sendLocalMessage(Text.literal("No list data")
                            .withColor(Formatting.YELLOW.getColorValue()), Prepend.DEFAULT.get(), false);
                return;
            }
            MutableText listMessage = Text.literal(name.substring(0, 1)
                            .toUpperCase() + name.substring(1) + " list page " + (page + 1) + ":\n")
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            for (int i = 5 * page; i < 5 * (page + 1); i++) {
                if (i >= res.size()) {
                    break;
                }
                listMessage.append(Text.literal(i + 1 + ". ")).withColor(0xFFFFFF);
                listMessage.append(lineParser.apply(res.get(i), sortMember));
                if (i != Math.min(page, res.size()) - 1) {
                    listMessage.append(Text.literal("\n"));
                }
            }
            boolean hasPrev = page > 0;
            boolean hasNext = 5 * (page + 1) < res.size();
            listMessage.append("\n");
            listMessage.append(Text.literal("<< Prev")
                            .setStyle(Style.EMPTY.withColor(hasPrev ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasPrev ? new ClickEvent.RunCommand("/" + name + "list view " + page + " false"):null)))
                    .append("          ").append(Text.literal("Next >>")
                            .setStyle(Style.EMPTY.withColor(hasNext ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasNext ? new ClickEvent.RunCommand("/" + name + "list view " + (page + 2) + " false"):null)));
            listMessage.append("\n");
            McUtils.sendLocalMessage(listMessage, Prepend.DEFAULT.get(), false);
        });
    }
}
