package kgg.translator.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface TranslateChatEvent {

    Event<TranslateChatEvent> EVENT = EventFactory.createArrayBacked(TranslateChatEvent.class,
            (listeners) -> (chat) -> {
                for (TranslateChatEvent event : listeners) {
                    String a = event.chat(chat);
                    if (a != null) {
                        return a;
                    }
                }
                return chat;
            });

    String chat(String text);
}
