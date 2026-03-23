package pixlze.guildapi.features;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.config.Configurable;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.ScreenEvents;
import pixlze.guildapi.mc.mixin.accessors.ScreenInvoker;
import pixlze.guildapi.screens.widgets.InfoPanelWidget;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoPanelsFeature extends Feature {
    private static final Pattern MEMBERS_SCREEN_PATTERN = Pattern.compile("^.+: Members$");
    private static final Pattern ASPECT_PATTERN = Pattern.compile("^§.(?<giver>.*?)(§.)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$");
    private static final Pattern TOME_PATTERN = Pattern.compile("^§.(?<giver>.*?)(§.)? rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$");
    @Configurable
    public final Config<Boolean> aspectsEnabled = new Config<>(true);
    @Configurable
    public final Config<Boolean> tomesEnabled = new Config<>(true);

    private final InfoPanelWidget aspectsPanel = new InfoPanelWidget("aspects", 10, 10, 100, 150, "Aspects", "guilds/raids/rewards/", (button) -> {
        aspectsEnabled.setPending(false);
        aspectsEnabled.applyPending();
        Managers.Config.saveConfig();
    },
            (element) -> {
                return new InfoPanelWidget.Entry(element.getAsJsonObject().get("mcUsername").getAsString(), element.getAsJsonObject().get("aspects").getAsDouble());
            });
    private final InfoPanelWidget tomesPanel = new InfoPanelWidget(null, 10, 10, 100, 150, "Tomes", "guilds/tomes/", (button) -> {
        tomesEnabled.setPending(false);
        tomesEnabled.applyPending();
        Managers.Config.saveConfig();
    },
            (element) -> {
                return new InfoPanelWidget.Entry(element.getAsJsonObject().get("mcUsername").getAsString(), 1);
            });

    private Screen currentScreen;


    public InfoPanelsFeature() {
        super("Info Panels");
    }

    @Override
    public void init() {
        ScreenEvents.CHANGE.register((screen) -> onScreenChanged(screen, true));
        ScreenEvents.RESIZE.register((screen) -> onScreenChanged(screen, false));
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    @Override
    public void onConfigUpdate(Config<?> config) {
        if (currentScreen == null) return;
        if (config.getName().equals("aspectsEnabled")) {
            if (config.getValue() instanceof Boolean val) {
                if (val)
                    ((ScreenInvoker) currentScreen).invokeAddDrawableChild(aspectsPanel);
                else {
                    McUtils.sendLocalMessage(Text.literal("§aYou can re-enable this panel in the /gapi config menu."), Prepend.DEFAULT.get(), false);
                    ((ScreenInvoker) currentScreen).invokeRemove(aspectsPanel);
                }
            }
        }
        if (config.getName().equals("tomesEnabled")) {
            if (config.getValue() instanceof Boolean val) {
                if (val)
                    ((ScreenInvoker) currentScreen).invokeAddDrawableChild(tomesPanel);
                else {
                    McUtils.sendLocalMessage(Text.literal("§aYou can re-enable this panel in the /gapi config menu."), Prepend.DEFAULT.get(), false);
                    ((ScreenInvoker) currentScreen).invokeRemove(tomesPanel);
                }
            }
        }
    }

    private void putAspects(Screen screen, boolean refresh) {
        if (!aspectsEnabled.getValue()) return;
        if (refresh) {aspectsPanel.refresh();}
        ((ScreenInvoker) screen).invokeAddDrawableChild(aspectsPanel);
    }

    // TODO: updating
    private void putTomes(Screen screen, boolean refresh) {
        if (!tomesEnabled.getValue()) return;
        if (refresh) {tomesPanel.refresh();}
        tomesPanel.setX(screen.width - tomesPanel.getWidth() - 10);
        ((ScreenInvoker) screen).invokeAddDrawableChild(tomesPanel);
    }

    private void putList(Screen screen, boolean refresh) {
        putAspects(screen, refresh);
        putTomes(screen, refresh);
    }

    private void onScreenChanged(Screen screen, boolean refresh) {
        if (screen == null || enabled.isDisabled()) return;
        currentScreen = screen;
        if (MEMBERS_SCREEN_PATTERN.matcher(TextUtils.parsePlain(screen.getTitle())).matches()) {
            putList(screen, refresh);
        }
    }

    private void onWynnMessage(Text message) {
        String asString = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        Matcher m1 = ASPECT_PATTERN.matcher(asString);
        Matcher m2 = TOME_PATTERN.matcher(asString);
        if (m1.find()) {
            aspectsPanel.update(m1.group("receiver"), -1);
        } else if (m2.find()) {
            tomesPanel.update(m1.group("receiver"), -1);
        }
    }
}
