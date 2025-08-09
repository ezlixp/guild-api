package pixlze.guildapi.screens.discord;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.discord.DiscordMessageManager;
import pixlze.guildapi.screens.discord.widgets.DiscordChatWidget;
import pixlze.guildapi.utils.McUtils;

public class DiscordChatScreen extends Screen {
    private final Screen parent;
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final TextFieldWidget discordInput = new TextFieldWidget(McUtils.mc().textRenderer, this.width, 25, Text.of("Message to discord"));
    public DiscordChatWidget body;

    public DiscordChatScreen(Screen parent) {
        super(Text.of("Discord"));
        this.parent = parent;
    }

    @Override
    public void init() {
        // TODO: improve design, make text field expand for each new line added like discord, increase size of footer
        // edit box widget
        discordInput.setFocused(false);
        discordInput.setFocusUnlocked(false);
        discordInput.setMaxLength(2000);
        this.initHeader();
        this.initBody();
        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
        this.body.setScrollY(this.body.getMaxScrollY());
        this.setInitialFocus();
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public void removed() {
        Managers.Discord.setDiscordChat(null);
    }

    protected void initHeader() {
        this.layout.addHeader(this.title, this.textRenderer);
    }

    protected void initBody() {
        this.body = this.layout.addBody(new DiscordChatWidget(McUtils.mc(), this.width, this));
        Managers.Discord.setDiscordChat(this.body);
        Managers.Discord.addAll(this.body);
    }

    @Override
    protected void setInitialFocus() {
        if (!Managers.DiscordSocket.isDisabled())
            this.setInitialFocus(discordInput);
        else {
            discordInput.setTooltip(Tooltip.of(Text.of("Discord bridging is not enabled. Please enable the feature and try again. If it is enabled, try typing /reconnect in chat.")));
        }
    }

    protected void initFooter() {
        discordInput.setWidth(this.width);
        this.layout.addFooter(discordInput);
    }

    @Override
    protected void refreshWidgetPositions() {
        discordInput.setWidth(this.width);
        this.layout.refreshPositions();
        if (this.body != null)
            this.body.position(this.width, this.layout);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            // TODO: disable input field if socket not on, and add tooltip
            String author = McUtils.playerName();
            String content = Managers.Discord.stripIllegal(discordInput.getText());
            if (content.isBlank()) return true;
            Managers.DiscordSocket.emit("discordOnlyWynnMessage", author + ": " + content);
            Managers.Discord.newMessage(author, "@me", content, false, DiscordMessageManager.DISCORD_MESSAGE);
            discordInput.setText("");
            this.body.setScrollY(this.body.getMaxScrollY());
            return true;
//        } else if (keyCode == GLFW.GLFW_KEY_UP) {
//            this.setChatFromHistory(-1);
//            return true;
//        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
//            this.setChatFromHistory(1);
//            return true;
//        } else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
//            this.client.inGameHud.getChatHud().scroll(this.client.inGameHud.getChatHud().getVisibleLineCount() - 1);
//            return true;
//        } else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
//            this.client.inGameHud.getChatHud().scroll(-this.client.inGameHud.getChatHud().getVisibleLineCount() + 1);
//            return true;
        } else {
            return false;
        }
    }
}
