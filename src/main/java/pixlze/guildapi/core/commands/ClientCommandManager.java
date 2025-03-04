package pixlze.guildapi.core.commands;

import pixlze.guildapi.core.Manager;

public class ClientCommandManager extends Manager {
    public void init() {
        // register all commands similar to feature
    }

    private void registerCommand(ClientCommand command) {
        command.init();
        command.register();
    }
}
