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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class ChatHandler extends Handler {
    private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("^\\s*(§r|À+)?\\s*$");
    private static final Pattern NPC_CONFIRM_PATTERN =
            Pattern.compile("^ *§[47]Press §[cf](SNEAK|SHIFT) §[47]to continue$");
    private static final Pattern NPC_SELECT_PATTERN =
            Pattern.compile("^ *§[47cf](Select|CLICK) §[47cf]an option (§[47])?to continue$");
    private static final int CHAT_SCREEN_TICK_DELAY = 1;

    private ArrayList<Text> collectedLines = new ArrayList<>();
    private long chatScreenTicks = 0;
    private LinkedList<Text> lastCollected = new LinkedList<>();

    @Override
    public void init() {
        WynnChatMessage.EVENT.register(this::onWynnMessage);
        WorldStateEvents.CHANGE.register(this::onConnectionChange);
    }

    private void onConnectionChange(WorldState state) {
        GuildApi.LOGGER.info("CONNECTION CHANGE");
        lastCollected = new LinkedList<>();
        collectedLines = new ArrayList<>();
        chatScreenTicks = 0;
    }

    private void onWynnMessage(Text message) {
        assert McUtils.mc().world != null;
        long currentTicks = McUtils.mc().world.getTime();

        List<Text> lines = TextUtils.splitLines(message);
        handleLines(lines, message, currentTicks);
    }

    private synchronized void handleLines(List<Text> lines, Text message, long currentTicks) {
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
            lastCollected.addLast(message);
            while (lastCollected.size() > 5) lastCollected.removeFirst();
            postChatLine(message);
        }
    }

    // add back last real message for first chat screen checking
    private synchronized void processCollected() {
        ArrayList<Text> filteredCollected = new ArrayList<>();
        for (int i = 0; i < collectedLines.size(); i++) {
            Text line = collectedLines.get(i);
            filteredCollected.add(line);
            if ((NPC_CONFIRM_PATTERN.matcher(TextUtils.parseStyled(line, TextParseOptions.DEFAULT)).find() ||
                    NPC_SELECT_PATTERN.matcher(TextUtils.parseStyled(line, TextParseOptions.DEFAULT)).find() ||
                    (i >= 3 &&
                            EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.get(i))).find() &&
                            EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.get(i - 1))).find() &&
                            EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.get(i - 2))).find() &&
                            EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.get(i - 3))).find()
                    )
            ) && i != collectedLines.size() - 1) {
                filteredCollected.clear();
            }
        }

        collectedLines = filteredCollected;
        if (collectedLines.isEmpty()) return;
        if (NPC_CONFIRM_PATTERN.matcher(TextUtils.parseStyled(collectedLines.getLast(), TextParseOptions.DEFAULT)).find()) {
            if (collectedLines.size() < 4) {
                GuildApi.LOGGER.warn("Unable to safely remove 4 lines for npc confirm pattern.");
                return;
            }
            for (int i = 0; i < 4; i++) {
                collectedLines.removeLast();
            }
        } else if (NPC_SELECT_PATTERN.matcher(TextUtils.parseStyled(collectedLines.getLast(), TextParseOptions.DEFAULT)).find()) {
            try {
                collectedLines.removeLast();
                do {
                    collectedLines.removeLast();
                } while (!EMPTY_LINE_PATTERN.matcher(TextUtils.parseStyled(collectedLines.getLast(), TextParseOptions.DEFAULT)).find());
                collectedLines.removeLast();
                collectedLines.removeLast();
                collectedLines.removeLast();
            } catch (Exception e) {
                GuildApi.LOGGER.warn("Unable to safely remove lines for npc select pattern.");
                return;
            }
        } else if (collectedLines.size() > 4) {
            if (EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.getLast())).find() &&
                    EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.get(collectedLines.size() - 2))).find() &&
                    EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.get(collectedLines.size() - 3))).find() &&
                    EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(collectedLines.get(collectedLines.size() - 4))).find()
            ) {
                collectedLines.removeLast();
                collectedLines.removeLast();
                collectedLines.removeLast();
                collectedLines.removeLast();
            }
        }

        filteredCollected = new ArrayList<>();
        for (Text line : collectedLines) {
            if (!EMPTY_LINE_PATTERN.matcher(TextUtils.parsePlain(line)).find()) filteredCollected.add(line);
        }
        collectedLines = filteredCollected;

        if (collectedLines.size() % 2 == 0) {
            boolean doubled = true;
            filteredCollected = new ArrayList<>();
            for (int i = 0; i < collectedLines.size() / 2; i++) {
                if (!TextUtils.parsePlain(collectedLines.get(i)).equals(TextUtils.parsePlain(collectedLines.get(i + collectedLines.size() / 2)))) {
                    doubled = false;
                    break;
                }
                filteredCollected.add(collectedLines.get(i));
            }
            if (doubled) {
                collectedLines = filteredCollected;
            }
        }


        for (Text line : collectedLines) {
            GuildApi.LOGGER.info("collected: {} {}", TextUtils.parsePlain(line), TextUtils.parseStyled(line, TextParseOptions.DEFAULT));
        }
        GuildApi.LOGGER.info("collected spacer: \n\n\n");
        if (collectedLines.isEmpty()) return;

        LinkedList<Text> newLines = new LinkedList<>();
        for (int i = 0; i < lastCollected.size(); i++) {
            int index = 0;
            boolean works = true;
            while (i + index < lastCollected.size() && index < collectedLines.size()) {
                if (!TextUtils.parsePlain(lastCollected.get(i + index)).equals(TextUtils.parsePlain(collectedLines.get(index)))) {
                    works = false;
                    break;
                }
                ++index;
            }
            if (works) {
                int extra = collectedLines.size() - lastCollected.size() + i;
                for (int j = collectedLines.size() - 1; j >= collectedLines.size() - extra; j--) {
                    newLines.addFirst(collectedLines.get(j));
                }
                break;
            }
        }
        lastCollected = new LinkedList<>(collectedLines);
        processNewLines(newLines);
    }

    private void processNewLines(LinkedList<Text> newLines) {
        for (Text line : newLines) {
            GuildApi.LOGGER.info("newline: {}", TextUtils.parseStyled(line, TextParseOptions.DEFAULT));
        }
        if (!newLines.isEmpty())
            GuildApi.LOGGER.info("newline spacer: \n\n\n");
        for (Text line : newLines) {
            postChatLine(line);
        }
    }

    private void postChatLine(Text line) {
        ChatMessageReceived.EVENT.invoker().interact(line);
    }
}
