package pixlze.guildapi.core.notifications;

public abstract class Trigger {
    public final static class CHAT extends Trigger {
        public final String message;

        public CHAT(String message) {
            this.message = message;
        }
    }
}
