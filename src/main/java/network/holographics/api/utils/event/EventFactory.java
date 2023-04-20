package network.holographics.api.utils.event;

import network.holographics.api.actions.ClickType;
import network.holographics.api.holograms.Hologram;
import network.holographics.api.holograms.HologramPage;
import network.holographics.event.HolographicsReloadEvent;
import network.holographics.event.HologramClickEvent;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class EventFactory {

    public static boolean handleHologramClickEvent(Player player, Hologram hologram, HologramPage page, ClickType clickType, int entityId) {
        if (HologramClickEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return true;
        }

        HologramClickEvent event = new HologramClickEvent(player, hologram, page, clickType, entityId);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    public static void handleReloadEvent() {
        if (HolographicsReloadEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }

        HolographicsReloadEvent event = new HolographicsReloadEvent();
        Bukkit.getPluginManager().callEvent(event);
    }

}
