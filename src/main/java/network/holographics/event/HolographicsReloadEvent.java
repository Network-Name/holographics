package network.holographics.event;

import org.bukkit.event.HandlerList;

public class HolographicsReloadEvent extends HolographicsEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public HolographicsReloadEvent() {
        super(true);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static boolean isRegistered() {
        return HANDLERS.getRegisteredListeners().length > 0;
    }

}
