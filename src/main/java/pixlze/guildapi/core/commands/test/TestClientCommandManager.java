package pixlze.guildapi.core.commands.test;

import pixlze.guildapi.commands.test.TestCommandHelpCommand;
import pixlze.guildapi.core.Manager;
import pixlze.guildapi.core.commands.ClientCommand;

public class TestClientCommandManager extends Manager {
    // hashmap of all commands, do init and registe rseperately
    public void init() {
        registerCommand(new TestCommandHelpCommand());
    }

    private void registerCommand(ClientCommand command) {
        command.init();
        command.register();
    }
}
