package pixlze.guildapi.mc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pixlze.guildapi.mc.mixin.accessors.ChatHudAccessorInvoker;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;

import java.util.List;


@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean doubled, CallbackInfoReturnable<boolean[]> ci) {
        assert client != null;
        assert client.currentScreen != null;
        ChatHud chatHud = client.inGameHud.getChatHud();
        ChatHudAccessorInvoker chatHudAccessorInvoker = (ChatHudAccessorInvoker) chatHud;

        int chatBottom = client.currentScreen.height - 40;
        int chatWidth = McUtils.getChatWidth();
        double lineHeight =
                chatHudAccessorInvoker.invokeGetLineHeight() * MinecraftClient.getInstance().options.getChatScale()
                        .getValue(); //
        // chat spacing


        double scrollOffset = chatHudAccessorInvoker.getScrolledLines();
        if (click.hasCtrl() || click.hasAlt() || click.hasShift()) {
            List<ChatHudLine> messages = chatHudAccessorInvoker.getMessages();
            int line = 0;
            for (ChatHudLine message : messages) {
                if (line > chatHud.getVisibleLineCount() + scrollOffset) break;

                int lines = ChatMessages.breakRenderedChatMessageLines(message.content(), chatWidth, textRenderer)
                        .size();
                if (line >= scrollOffset) {
                    if (click.x() <= chatWidth && click.y() <= chatBottom - lineHeight * (line - scrollOffset) && click.y() >= chatBottom - lineHeight * (line + lines - scrollOffset)) {
                        if (click.hasCtrl()) {
                            MinecraftClient.getInstance().keyboard.setClipboard(
                                    TextUtils.parsePlain(message.content()));
                        }
                        if (click.hasAlt()) {
                            MinecraftClient.getInstance().keyboard.setClipboard(
                                    TextUtils.parseStyled(message.content(), TextParseOptions.DEFAULT));
                        }
                        if (click.hasShift()) {
                            MinecraftClient.getInstance().keyboard.setClipboard(message.content().toString());
                        }
                    }
                }
                line += lines;
            }
            ci.cancel();
        }
    }
}