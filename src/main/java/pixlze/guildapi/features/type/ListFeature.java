package pixlze.guildapi.features.type;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ListFeature extends Feature {
    protected String endpoint;
    private final String name;
    private final BiFunction<JsonElement, String, MutableText> lineParser;
    private JsonElement cachedResponse;
    private String sortMember;
    private String extra;

    public ListFeature(String name, String endpoint, Function<JsonElement, MutableText> lineParser) {
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = (listItem, sortBy) -> lineParser.apply(listItem);
    }

    public ListFeature(String name, String endpoint, Function<JsonElement, MutableText> lineParser, String sortMember) {
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = (listItem, sortBy) -> lineParser.apply(listItem);
        this.sortMember = sortMember;
    }

    public ListFeature(String name, String endpoint, BiFunction<JsonElement, String, MutableText> lineParser) {
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = lineParser;
    }

    public ListFeature(String name, String endpoint, BiFunction<JsonElement, String, MutableText> lineParser, String sortMember) {
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
    public void init() {
        registerCommands(List.of());
    }

    public void registerCommands(List<LiteralArgumentBuilder<FabricClientCommandSource>> subCommands) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                    LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal(name + "list")
                            .executes(context -> {
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
                                            )));
                    for (LiteralArgumentBuilder<FabricClientCommandSource> subCommand : subCommands) {
                        builder.then(subCommand);
                    }
                    dispatcher.register(builder);
                }
        );
    }

    protected String getSortMember() {
        return this.sortMember;
    }

    protected void setSortMember(String sortMember) {
        this.sortMember = sortMember;
    }

    private void applySort(List<JsonElement> listElements) {
        if (sortMember == null) return;
        listElements.sort((a, b) -> {
            try {
                double val1 = a.getAsJsonObject().get(sortMember).getAsDouble();
                double val2 = b.getAsJsonObject().get(sortMember).getAsDouble();
                if (val1 < val2) return 1;
                else if (Math.abs(val1 - val2) < 0.0000001) return 0;
                else return -1;
            } catch (Exception error) {
                return 0;
            }
        });
    }

    private void listItems(int page, boolean reload) {
        CompletableFuture<JsonElement> response = new CompletableFuture<>();
        if (reload) {
            Managers.Net.guild.get(endpoint + getExtra()).whenCompleteAsync((res, exception) -> {
                try {
                    NetUtils.applyDefaultCallback(res, exception, response::complete, (error) -> response.completeExceptionally(null));
                } catch (Exception e) {
                    response.completeExceptionally(e);
                }
            });
        } else response.complete(cachedResponse);
        response.whenCompleteAsync((res, exception) -> {
            if (exception != null) {
                McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                GuildApi.LOGGER.error("List feature error: {} {}", exception, exception.getMessage());
                return;
            }
            cachedResponse = res;
            if (res == null) {
                assert Formatting.YELLOW.getColorValue() != null;
                if (!reload) McUtils.sendLocalMessage(Text.literal("No list data")
                        .withColor(Formatting.YELLOW.getColorValue()), Prepend.DEFAULT.get(), false);
                return;
            }
            List<JsonElement> listItems = res.getAsJsonArray().asList();
            applySort(listItems);
            MutableText listMessage = Text.literal(name.substring(0, 1)
                            .toUpperCase() + name.substring(1) + " list page " + (page + 1) + ":\n")
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            for (int i = 5 * page; i < 5 * (page + 1); i++) {
                if (i >= listItems.size()) {
                    break;
                }
                listMessage.append(Text.literal(i + 1 + ". ")).withColor(0xFFFFFF);
                listMessage.append(lineParser.apply(listItems.get(i), sortMember));
                if (i != Math.min(page, listItems.size()) - 1) {
                    listMessage.append(Text.literal("\n"));
                }
            }
            boolean hasPrev = page > 0;
            boolean hasNext = 5 * (page + 1) < listItems.size();
            listMessage.append("\n");
            listMessage.append(Text.literal("<< Prev")
                            .setStyle(Style.EMPTY.withColor(hasPrev ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasPrev ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + name + "list view " + page + " false"):null)))
                    .append("          ").append(Text.literal("Next >>")
                            .setStyle(Style.EMPTY.withColor(hasNext ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasNext ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + name + "list view " + (page + 2) + " false"):null)));
            listMessage.append("\n");
            McUtils.sendLocalMessage(listMessage, Prepend.DEFAULT.get(), false);
        });
    }
}
