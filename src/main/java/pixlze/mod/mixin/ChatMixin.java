package pixlze.mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.StringVisitable;
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
import pixlze.utils.Visitors;

import java.util.ArrayList;
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

                int lines = 0;
                List<Text> messageParts = message.content().getWithStyle(message.content().getStyle());
                List<StringVisitable> pieces = new ArrayList<>();
                List<Text> currentVisitable = new ArrayList<>();

                for (Text part : messageParts) {
                    String literal = part.getLiteralString();
                    String string = part.getString();
                    String content = null;
                    if (literal != null) {
                        content = literal;
                    } else if (string != null) {
                        content = string;
                    }

                    if (content != null) {
                        int index = 0;
                        int occ = content.indexOf("\n", index);
                        while (occ != -1) {
                            String piece = content.substring(index, occ);
                            if (piece.isEmpty()) piece = "\u200B";

                            currentVisitable.add(Text.literal(piece).setStyle(part.getStyle()));

                            pieces.add(StringVisitable.concat(currentVisitable));
                            currentVisitable = new ArrayList<>();

                            index = occ + 1;
                            occ = content.indexOf("\n", index);
                        }
                        if (index < content.length()) {
                            String piece = content.substring(index);
                            if (piece.isEmpty()) piece = "\u200B";
                            currentVisitable.add(Text.literal(piece).setStyle(part.getStyle()));
                        } else if (index == content.length()) {
                            currentVisitable.add(Text.literal("\u200B").setStyle(part.getStyle()));
                        }
                    } else
                        PixUtils.LOGGER.warn("{} does not have content", part);

                }
                if (!currentVisitable.isEmpty()) {
                    pieces.add(StringVisitable.concat(currentVisitable));
                }
                for (StringVisitable piece : pieces) {
                    lines += textRenderer.getTextHandler().wrapLines(piece, chatWidth, message.content().getStyle()).size();
                }

                if (line >= scrollOffset) {
                    if (mouseX <= chatWidth && mouseY <= chatBottom - lineHeight * (line - scrollOffset) && mouseY >= chatBottom - lineHeight * (line + lines - scrollOffset)) {
                        if (CopyChat.config.getValue()) {
                            if (Screen.hasControlDown()) {
                                Visitors.currentVisit = new StringBuilder();
                                message.content().visit(Visitors.PLAIN_VISITOR, message.content().getStyle());
                                MinecraftClient.getInstance().keyboard.setClipboard(Visitors.currentVisit.toString());
                            }
                            if (Screen.hasAltDown()) {
                                Visitors.currentVisit = new StringBuilder();
                                message.content().visit(Visitors.STYLED_VISITOR, message.content().getStyle());
                                MinecraftClient.getInstance().keyboard.setClipboard(Visitors.currentVisit.toString());
                            }
                            if (Screen.hasShiftDown()) {
                                MinecraftClient.getInstance().keyboard.setClipboard(message.content().toString());
                                Visitors.currentVisit = new StringBuilder();
                                message.content().visit(Visitors.RAID_VISITOR, message.content().getStyle());
                                PixUtils.LOGGER.info("{} with raid visitor. the message has {} lines", Visitors.currentVisit, lines);
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