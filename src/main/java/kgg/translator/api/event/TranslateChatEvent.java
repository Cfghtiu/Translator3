package kgg.translator.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Optional;

public interface TranslateChatEvent {
    public static final Event<TranslateChatEvent> EVENT = EventFactory.createArrayBacked(TranslateChatEvent.class, (callbacks) -> (text) -> {
        Optional<String> ret = Optional.empty();
        for (TranslateChatEvent callback : callbacks) {
            ret = ret.or(() -> callback.onTranslateChat(text));
        }
        return ret;
    });

    Optional<String> onTranslateChat(String text);
}
