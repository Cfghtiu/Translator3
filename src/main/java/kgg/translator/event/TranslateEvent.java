package kgg.translator.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;


public class TranslateEvent {
    public static Event<Begin> BEGIN = EventFactory.createArrayBacked(Begin.class, (listeners) -> (text, from, to, source) -> {
        for (Begin listener : listeners) {
            if (!listener.begin(text, from, to, source)) {
                return false;
            }
        }
        return true;
    });

    public static Event<After> AFTER = EventFactory.createArrayBacked(After.class, (listeners) -> (text, result, from, to, source) -> {
        for (After listener : listeners) {
            String a = listener.after(text, result, from, to, source);
            if (a != null) {
                result = a;
            }
        }
        return result;
    });

    public interface Begin {
        boolean begin(String text, String from, String to, String source);
    }

    public interface After {
        String after(String text, String result, String from, String to, String source);
    }
}
