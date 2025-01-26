package kgg.translator.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.widget.OptionListWidget;

public interface OptionsScreenAddBodyEvent {
    Event<OptionsScreenAddBodyEvent> EVENT = EventFactory.createArrayBacked(OptionsScreenAddBodyEvent.class,
        (listeners) -> (body) -> {
            for (OptionsScreenAddBodyEvent listener : listeners) {
                listener.add(body);
            }
        });

    void add(OptionListWidget body);
}
