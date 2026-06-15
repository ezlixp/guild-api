package pixlze.guildapi.features;

import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.config.Config;

public class FindHubFeature extends Feature {

    public FindHubFeature() {
        super("Guild Location Sharing");
    }

    @Override
    public void init() {
//        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    @Override
    public void onConfigUpdate(Config<?> config) {

    }

}
