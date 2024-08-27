package pixlze.mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pixlze.mod.PixUtils;
import pixlze.mod.features.copy_chat.CopyChat;
import pixlze.mod.mixin.accessors.ChatHudAccessorInvoker;

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
        if (Screen.hasControlDown() || Screen.hasAltDown()) {
            List<ChatHudLine> messages = chatHudAccessorInvoker.getMessages();
            int line = 0;
            for (ChatHudLine message : messages) {
                if (line > chatHud.getVisibleLineCount() + scrollOffset) break;
                int lines = textRenderer.getTextHandler().wrapLines(message.content(), (int) chatWidth, message.content().getStyle()).size();
                if (line >= scrollOffset) {
                    if (mouseX <= chatWidth && mouseY <= chatBottom - lineHeight * (line - scrollOffset) && mouseY >= chatBottom - lineHeight * (line + lines - scrollOffset)) {
                        if (CopyChat.config.getValue()) {
                            if (Screen.hasControlDown())
                                MinecraftClient.getInstance().keyboard.setClipboard(message.content().getString());
                            if (Screen.hasAltDown()) {
                                PixUtils.currentVisit = "";
                                message.content().visit(PixUtils.wynnVisitor, message.content().getStyle());
                                MinecraftClient.getInstance().keyboard.setClipboard(PixUtils.currentVisit);
                            }
                        }
                    }
                }
                line += lines;
            }
            info.cancel();
        }
        if (Screen.hasShiftDown()) {
            PixUtils.LOGGER.info("{}, {}", mouseX, mouseY);
            PixUtils.LOGGER.info("chatwidth: {}, scrollOffset: {}, lineHeight: {}, scale: {}", chatWidth, scrollOffset, lineHeight, MinecraftClient.getInstance().options.getChatScale().getValue());
        }
    }
}