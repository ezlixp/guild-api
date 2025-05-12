package pixlze.guildapi.core.notifications;

public abstract class NotificationTrigger {
    public final static class CHAT extends NotificationTrigger {
        public final String message;

        public CHAT(String message) {
            this.message = message;
        }
    }
}
