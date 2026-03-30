package pixlze.guildapi.features;

import com.google.gson.JsonElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.config.Configurable;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.ScreenEvents;
import pixlze.guildapi.mc.mixin.accessors.ScreenInvoker;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.models.guildMessage.type.GuildMessage;
import pixlze.guildapi.screens.widgets.InfoPanelWidget;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoPanelsFeature extends Feature {
    private static final Pattern MEMBERS_SCREEN_PATTERN = Pattern.compile("^.+: Members$");
    // maybe make these configs
    private static final double ASPECTS_THRESHOLD = 1;
    private static final double TOMES_THRESHOLD = 1;

    @Configurable
    public final Config<Boolean> aspectsEnabled = new Config<>(true);
    @Configurable
    public final Config<Boolean> tomesEnabled = new Config<>(true);

    private final List<InfoPanelWidget.Entry> aspectsList = new ArrayList<>();
    private final List<InfoPanelWidget.Entry> tomesList = new ArrayList<>();

    private final ButtonWidget.PressAction aspectsOnPress = (button) -> {
        aspectsEnabled.setPending(Boolean.FALSE);
        aspectsEnabled.applyPending();
        Managers.Config.saveConfig();
    };
    private final ButtonWidget.PressAction tomesOnPress = (button) -> {
        tomesEnabled.setPending(Boolean.FALSE);
        tomesEnabled.applyPending();
        Managers.Config.saveConfig();
    };

    private InfoPanelWidget aspectsPanel;
    private InfoPanelWidget tomesPanel;
    private Screen currentScreen;


    public InfoPanelsFeature() {
        super("Info Panels");
    }

    @Override
    public void init() {
        ScreenEvents.CHANGE.register((screen) -> onScreenChanged(screen, true));
        ScreenEvents.RESIZE.register((screen) -> onScreenChanged(screen, false));
        ScreenEvents.SLOT.register(this::onSlotDrawn);
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    @Override
    public void onConfigUpdate(Config<?> config) {
        if (currentScreen == null) return;
        // enabling should never instantly make it show, so we are safe to only remove here
        // in the future might change if i add a button to the guild manage screen to open them (could be useful if u accidentally close it)
        if (config.getName().equals("aspectsEnabled")) {
            if (config.getValue() instanceof Boolean val) {
                if (!val) {
                    McUtils.sendLocalMessage(Text.literal("§aYou can re-enable this panel in the /gapi config menu."), Prepend.DEFAULT.get(), false);
                    ((ScreenInvoker) currentScreen).invokeRemove(aspectsPanel);
                    aspectsPanel = null;
                }
            }
        }
        if (config.getName().equals("tomesEnabled")) {
            if (config.getValue() instanceof Boolean val) {
                if (!val) {
                    McUtils.sendLocalMessage(Text.literal("§aYou can re-enable this panel in the /gapi config menu."), Prepend.DEFAULT.get(), false);
                    ((ScreenInvoker) currentScreen).invokeRemove(tomesPanel);
                    tomesPanel = null;
                }
            }
        }
    }

    private void refreshAspects() {
        aspectsList.clear();
        Managers.Net.guild.getList("guilds/raids/rewards/" + Managers.Net.guild.guildId, false, "aspects").whenComplete((res, exception) -> {
            if (exception != null) {
                McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                GuildApi.LOGGER.error("Info list aspects refresh error: {} {}", exception, exception.getMessage());
                return;
            }
            for (JsonElement element : res) {
                InfoPanelWidget.Entry t = new InfoPanelWidget.Entry(element.getAsJsonObject().get("mcUsername").getAsString(), element.getAsJsonObject().get("aspects").getAsDouble());
                if (t.value >= ASPECTS_THRESHOLD)
                    aspectsList.add(t);
            }
            aspectsPanel.changePage(0);
        });
    }

    private void refreshTomes() {
        tomesList.clear();
        Managers.Net.guild.getList("guilds/tomes/" + Managers.Net.guild.guildId, false, null).whenComplete((res, exception) -> {
            if (exception != null) {
                McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                GuildApi.LOGGER.error("Info list tomes refresh error: {} {}", exception, exception.getMessage());
                return;
            }
            for (JsonElement element : res) {
                InfoPanelWidget.Entry t = new InfoPanelWidget.Entry(element.getAsJsonObject().get("mcUsername").getAsString(), 1);
                if (t.value >= TOMES_THRESHOLD)
                    tomesList.add(t);
            }
            tomesPanel.changePage(0);
        });
    }

    private void putAspects(GenericContainerScreen screen, boolean refresh) {
        if (!aspectsEnabled.getValue()) return;
        if (refresh) {refreshAspects();}
        aspectsPanel = new InfoPanelWidget(10, 10, 100, 150, "Aspects", aspectsOnPress, aspectsList, ASPECTS_THRESHOLD);
        aspectsPanel.setHighlightColour(0xAAFF0000);
        ((ScreenInvoker) screen).invokeAddDrawableChild(aspectsPanel);
    }

    private void putTomes(GenericContainerScreen screen, boolean refresh) {
        if (!tomesEnabled.getValue()) return;
        if (refresh) {refreshTomes();}
        tomesPanel = new InfoPanelWidget(screen.width - 110, 10, 100, 150, "Tomes", tomesOnPress, tomesList, TOMES_THRESHOLD);
        tomesPanel.setHighlightColour(0xAA0000FF);
        ((ScreenInvoker) screen).invokeAddDrawableChild(tomesPanel);
    }

    private void putList(GenericContainerScreen screen, boolean refresh) {
        putAspects(screen, refresh);
        putTomes(screen, refresh);
    }

    private void onScreenChanged(Screen screen, boolean refresh) {
        if (screen == null || enabled.isDisabled()) return;
        if (screen instanceof GenericContainerScreen containerScreen && MEMBERS_SCREEN_PATTERN.matcher(TextUtils.parsePlain(screen.getTitle())).matches()) {
            currentScreen = screen;
            putList(containerScreen, refresh);
        } else {
            currentScreen = null;
        }
    }

    private void onWynnMessage(Text message) {
        String asString = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        String content = Models.GuildMessage.getContent(asString);
        Matcher m1 = GuildMessage.ASPECT_GIVE.getMatcher(content);
        Matcher m2 = GuildMessage.TOME_GIVE.getMatcher(content);
        if (aspectsPanel != null && m1 != null) {
            aspectsPanel.update(m1.group("receiver"), -1);
        } else if (tomesPanel != null && m2 != null) {
            tomesPanel.update(m2.group("receiver"), -1);
        }
    }

    private void onSlotDrawn(DrawContext context, Slot slot) {
        if (aspectsPanel != null) aspectsPanel.onSlotDrawn(context, slot);
        if (tomesPanel != null) tomesPanel.onSlotDrawn(context, slot);
    }
}
