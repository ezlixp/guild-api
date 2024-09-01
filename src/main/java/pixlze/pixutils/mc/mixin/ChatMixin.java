package pixlze.pixutils.mc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pixlze.pixutils.core.PixUtils;
import pixlze.pixutils.features.copy_chat.CopyChat;
import pixlze.pixutils.mc.mixin.accessors.ChatHudAccessorInvoker;
import pixlze.pixutils.utils.ChatUtils;

import java.util.List;


@Mixin(ChatScreen.class)
public abstract class ChatMixin extends Screen {
    @Unique
    private int count = 0;


    protected ChatMixin(Text title) {
        super(title);
    }

    @Shadow
    public void sendMessage(String chatText, boolean addToHistory) {
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<boolean[]> info) {
        if (keyCode != 342 && keyCode != 346 && (modifiers & 4) != 0) {
            sendMessage(String.valueOf(count), false);
            ++count;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<boolean[]> info) {
        assert client != null;
        assert client.currentScreen != null;
        ChatHud chatHud = client.inGameHud.getChatHud();
        ChatHudAccessorInvoker chatHudAccessorInvoker = (ChatHudAccessorInvoker) chatHud;

        int chatBottom = client.currentScreen.height - 40; // height adjust
        int chatWidth = chatHudAccessorInvoker.invokeGetWidth();
        double lineHeight = chatHudAccessorInvoker.invokeGetLineHeight() * MinecraftClient.getInstance().options.getChatScale().getValue(); // chat spacing


        double scrollOffset = chatHudAccessorInvoker.getScrolledLines();
        if (Screen.hasControlDown() || Screen.hasAltDown() || Screen.hasShiftDown()) {
            List<ChatHudLine> messages = chatHudAccessorInvoker.getMessages();
            int line = 0;
            for (ChatHudLine message : messages) {
                if (line > chatHud.getVisibleLineCount() + scrollOffset) break;

                int lines = ChatMessages.breakRenderedChatMessageLines(message.content(), chatWidth, textRenderer).size();
                if (line >= scrollOffset) {
                    if (mouseX <= chatWidth && mouseY <= chatBottom - lineHeight * (line - scrollOffset) && mouseY >= chatBottom - lineHeight * (line + lines - scrollOffset)) {
                        if (CopyChat.config.getValue()) {
                            if (Screen.hasControlDown()) {
                                ChatUtils.currentVisit = new StringBuilder();
                                message.content().visit(ChatUtils.PLAIN_VISITOR, message.content().getStyle());
                                MinecraftClient.getInstance().keyboard.setClipboard(ChatUtils.currentVisit.toString());
                            }
                            if (Screen.hasAltDown()) {
                                ChatUtils.currentVisit = new StringBuilder();
                                message.content().visit(ChatUtils.STYLED_VISITOR, message.content().getStyle());
                                MinecraftClient.getInstance().keyboard.setClipboard(ChatUtils.currentVisit.toString());
                            }
                            if (Screen.hasShiftDown()) {
                                MinecraftClient.getInstance().keyboard.setClipboard(message.content().toString());
                                ChatUtils.currentVisit = new StringBuilder();
                                message.content().visit(ChatUtils.RAID_VISITOR, message.content().getStyle());
                                PixUtils.LOGGER.info("{} with raid visitor. the message has {} lines", ChatUtils.currentVisit, lines);
                            }
                        }
                    }
                }
                line += lines;
            }
            info.cancel();
        }
    }
}