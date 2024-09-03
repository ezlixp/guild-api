package pixlze.guildapi.handlers.chat;

public final class ChatHandler {
//    private void onWynnMessage(Text message) {
//        String raidMessage = ChatUtils.parseRaid(message);
//        Matcher raidMatcher = Pattern.compile(".*§e(.*?)§b.*§e(.*?)§b.*§e(.*?)§b.*§e(.*?)§b.*?§3(.*?)§b").matcher(raidMessage);
//        if (raidMatcher.find() && !raidMessage.contains(":")) {
//            JsonObject requestBody = new JsonObject();
//            requestBody.add("users", GuildApi.gson.fromJson(Arrays.toString(new String[]{raidMatcher.group(1), raidMatcher.group(2), raidMatcher.group(3), raidMatcher.group(4)}), JsonElement.class));
//            requestBody.add("raid", GuildApi.gson.fromJson(raidMatcher.group(5), JsonElement.class));
//            requestBody.add("timestamp", GuildApi.gson.fromJson(String.valueOf(Instant.now().toEpochMilli()), JsonElement.class));
//            Managers.Api.Guild.post("addRaid", requestBody);
//        }
//    }
//
//    public void init() {
//        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
//    }
}
