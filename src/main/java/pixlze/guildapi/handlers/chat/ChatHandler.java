package pixlze.guildapi.handlers.chat;

import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.models.event.WorldStateEvents;
import pixlze.guildapi.models.type.WorldState;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class ChatHandler {
    private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("^\\s*(§r|À+)?\\s*$");
    private static final Pattern NPC_CONFIRM_PATTERN =
            Pattern.compile("^ *§[47]Press §[cf](SNEAK|SHIFT) §[47]to continue$");
    private static final Pattern NPC_SELECT_PATTERN =
            Pattern.compile("^ *§[47cf](Select|CLICK) §[47cf]an option (§[47])?to continue$");
    private ArrayList<Text> collectedLines = new ArrayList<>();
    private long chatScreenTicks = 0;
    private String lastRealChat = null;
    private String oneBeforeLastRealChat = null;

    public void init() {
        WynnChatMessage.EVENT.register(this::onWynnMessage);
        WorldStateEvents.CHANGE.register(this::onConnectionChange);
    }

    private void onWynnMessage(Text message) {
        assert McUtils.mc().world != null;
        long currentTicks = McUtils.mc().world.getTime();

        List<Text> lines = TextUtils.splitLines(message);
        if (lines.size() > 1 || (message.getString().isEmpty() && (currentTicks <= chatScreenTicks + 1))) {
            if (currentTicks <= chatScreenTicks + 1) {
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
        lastRealChat = null;
        oneBeforeLastRealChat = null;
        collectedLines = new ArrayList<>();
        chatScreenTicks = 0;
    }

    private void processCollected() {
        List<Text> lines = new ArrayList<>(collectedLines);
//        for (Text line : lines) {
//            if (!TextUtils.parsePlain(line).isBlank())
//                GuildApi.LOGGER.info("Collected line: {}", line.getString());
//        }
//        GuildApi.LOGGER.info("Collected line spacer: \n\n\n");

        collectedLines = new ArrayList<>();
        chatScreenTicks = 0;

        Collections.reverse(lines);

        LinkedList<Text> newLines = new LinkedList<>();
        if (lastRealChat == null) {
            lines.forEach(newLines::addLast);
        } else {
            for (Text line : lines) {
                String plainText = TextUtils.parsePlain(line);
                if (plainText.equals(lastRealChat)) break;
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
            } else if (newLines.size() == 4) {
                if (EMPTY_LINE_PATTERN.matcher(newLines.get(0).getString()).matches() &&
                        EMPTY_LINE_PATTERN.matcher(newLines.get(1).getString()).matches() &&
                        EMPTY_LINE_PATTERN.matcher(newLines.get(2).getString()).matches() &&
                        EMPTY_LINE_PATTERN.matcher(newLines.get(3).getString()).matches()
                ) {
                    expectedConfirmationlessDialogue = true;
                    newLines.removeFirst();
                    newLines.removeFirst();
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
        LinkedList<Text> newChatLines = new LinkedList<>();

        Text firstText = newLines.getFirst();
        boolean isNpcConfirm = NPC_CONFIRM_PATTERN.matcher(TextUtils.parseStyled(firstText, "§", "")).find();
        boolean isNpcSelect = NPC_SELECT_PATTERN.matcher(firstText.getString()).find();

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
                }

                newChatLines.addLast(line);
            }
        }
        newChatLines.forEach(this::postChatLine);
    }
}
