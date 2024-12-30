package pixlze.guildapi.handlers.chat;

import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Handler;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.models.worldState.event.WorldStateEvents;
import pixlze.guildapi.models.worldState.type.WorldState;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class ChatHandler extends Handler {
    private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("^\\s*(§r|À+)?\\s*$");
    private static final Pattern NPC_CONFIRM_PATTERN =
            Pattern.compile("^ *§[47]Press §[cf](SNEAK|SHIFT) §[47]to continue$");
    private static final Pattern NPC_SELECT_PATTERN =
            Pattern.compile("^ *§[47cf](Select|CLICK) §[47cf]an option (§[47])?to continue$");
    private static final int CHAT_SCREEN_TICK_DELAY = 3;

    private ArrayList<Text> collectedLines = new ArrayList<>();
    private long chatScreenTicks = 0;
    private String lastRealChat = null;
    private String oneBeforeLastRealChat = null;
    private String lastConfirmationlessDialogue = null;

    @Override
    public void init() {
        WynnChatMessage.EVENT.register(this::onWynnMessage);
        WorldStateEvents.CHANGE.register(this::onConnectionChange);
    }

    private void onWynnMessage(Text message) {
        assert McUtils.mc().world != null;
        long currentTicks = McUtils.mc().world.getTime();

        List<Text> lines = TextUtils.splitLines(message);
        if (lines.size() > 1 || (message.getString().isEmpty() && (currentTicks <= chatScreenTicks + CHAT_SCREEN_TICK_DELAY))) {
            if (currentTicks <= chatScreenTicks + CHAT_SCREEN_TICK_DELAY) {
                collectedLines.addAll(lines);
            } else {
                if (chatScreenTicks != 0) {
                    processCollected();
                }
                collectedLines = new ArrayList<>(lines);
                chatScreenTicks = currentTicks;
            }
        } else {
            if (chatScreenTicks != 0) {
                processCollected();
            }
            postChatLine(message);
        }
    }

    private void onConnectionChange(WorldState state) {
        GuildApi.LOGGER.info("CONNECTION CHANGE");
        lastRealChat = null;
        oneBeforeLastRealChat = null;
        lastConfirmationlessDialogue = null;
        collectedLines = new ArrayList<>();
        chatScreenTicks = 0;
    }

    private void processCollected() {
        GuildApi.LOGGER.info("{} {} last chats", oneBeforeLastRealChat, lastRealChat);
        List<Text> lines = new ArrayList<>(collectedLines);
        for (Text line : lines) {
            GuildApi.LOGGER.info("Collected line: {}", line.getString());
        }
        GuildApi.LOGGER.info("Collected line spacer: \n\n\n");

        collectedLines = new ArrayList<>();
        chatScreenTicks = 0;

        Collections.reverse(lines);

        LinkedList<Text> newLines = new LinkedList<>();
        if (lastRealChat == null) {
            lines.forEach(newLines::addLast);
        } else {
            for (Text line : lines) {
                String plainText = TextUtils.parsePlain(line);
                // TODO make this not filter duplicates
                if (plainText.equals(lastRealChat)) {
                    break;
                }
                if (plainText.equals(oneBeforeLastRealChat)) {
                    lastRealChat = oneBeforeLastRealChat;
                    oneBeforeLastRealChat = null;
                    break;
                }
                newLines.addLast(line);
            }
        }
        if (newLines.isEmpty()) return;

        boolean expectedConfirmationlessDialogue = false;

        if (newLines.getLast().getString().isEmpty()) {
            if (newLines.size() == 2) {
                if (EMPTY_LINE_PATTERN.matcher(newLines.getFirst().getString()).matches()) {
                    return;
                }

                expectedConfirmationlessDialogue = true;
                GuildApi.LOGGER.info("expecting confirmationless dialogue [#1]");
            } else if (newLines.size() == 4) {
                if (EMPTY_LINE_PATTERN.matcher(newLines.get(0).getString()).matches() &&
                        EMPTY_LINE_PATTERN.matcher(newLines.get(1).getString()).matches() &&
                        EMPTY_LINE_PATTERN.matcher(newLines.get(2).getString()).matches() &&
                        EMPTY_LINE_PATTERN.matcher(newLines.get(3).getString()).matches()
                ) {
                    expectedConfirmationlessDialogue = true;
                    newLines.removeFirst();
                    newLines.removeFirst();
                    GuildApi.LOGGER.info("expecting confirmationless dialogue [#2]");
                }
            }
            newLines.removeLast();
        }
        processNewLines(newLines, expectedConfirmationlessDialogue);
    }

    private void postChatLine(Text line) {
        if (!TextUtils.parsePlain(line).isBlank()) {
            oneBeforeLastRealChat = lastRealChat;
            lastRealChat = TextUtils.parsePlain(line);
        }
        ChatMessageReceived.EVENT.invoker().interact(line);
    }

    private void processNewLines(LinkedList<Text> newLines, boolean expectedConfirmationlessDialogue) {
//        for (Text line : newLines) {
//            if (!TextUtils.parseStyled(line, TextParseOptions.DEFAULT).isBlank()) {
//                GuildApi.LOGGER.info("newline: {}", TextUtils.parseStyled(line, TextParseOptions.DEFAULT));
//            }
//        }
//        GuildApi.LOGGER.info("newline spacer: \n\n\n");
        LinkedList<Text> newChatLines = new LinkedList<>();

        Text firstText = newLines.getFirst();
        boolean isNpcConfirm = NPC_CONFIRM_PATTERN.matcher(TextUtils.parseStyled(firstText, TextParseOptions.DEFAULT))
                .find();
        boolean isNpcSelect = NPC_SELECT_PATTERN.matcher(TextUtils.parseStyled(firstText, TextParseOptions.DEFAULT))
                .find();

        if (isNpcConfirm || isNpcSelect) {
            newLines.removeFirst();
            if (newLines.isEmpty()) {
                return;
            }
            if (newLines.getFirst().getString().isEmpty()) {
                newLines.removeFirst();
            } else {
                GuildApi.LOGGER.warn("Malformed dialog [#1]: {}", newLines.getFirst());
            }

            boolean dialogDone = false;
            boolean optionsFound = !isNpcSelect;

            for (Text line : newLines) {
                if (!dialogDone) {
                    if (EMPTY_LINE_PATTERN.matcher(line.getString()).find()) {
                        if (!optionsFound) {
                            optionsFound = true;
                        } else {
                            dialogDone = true;
                        }
                    }
                } else {
                    if (!EMPTY_LINE_PATTERN.matcher(line.getString()).find()) {
                        newChatLines.push(line);
                    }
                }
            }
        } else if (expectedConfirmationlessDialogue) {
            if (newLines.size() != 1) {
                GuildApi.LOGGER.warn("New lines has an unexpected dialogue count [#1]: {}", newLines);
            }
            lastConfirmationlessDialogue = TextUtils.parsePlain(newLines.getFirst());
            return;
        } else {
            while (!newLines.isEmpty() && EMPTY_LINE_PATTERN.matcher(newLines.getFirst().getString()).find()) {
                newLines.removeFirst();
            }
            Collections.reverse(newLines);
            while (!newLines.isEmpty()) {
                Text line = newLines.removeFirst();
                if (EMPTY_LINE_PATTERN.matcher(line.getString()).find()) {
                    if (newLines.isEmpty()) {
                        break;
                    }
                    Text nextLine = newLines.getFirst();
                    if (TextUtils.parsePlain(nextLine).equals(lastConfirmationlessDialogue)) {
                        if (newLines.size() > 1) {
                            GuildApi.LOGGER.warn("Unexpected lines after a confirmationless dialogue: {}", newLines);
                        }

                        break;
                    }
                }

                newChatLines.addLast(line);
            }
        }
        newChatLines.forEach(this::postChatLine);
    }
}
