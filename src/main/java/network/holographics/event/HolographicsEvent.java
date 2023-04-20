package network.holographics.event;

import org.bukkit.event.Event;

public abstract class HolographicsEvent extends Event {

    public HolographicsEvent() {
        super();
    }

    public HolographicsEvent(boolean isAsync) {
        super(isAsync);
    }

}
