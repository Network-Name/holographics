package network.holographics.api.player;

import network.holographics.api.HolographicsInternal;
import network.holographics.api.HolographicsInternalAPI;
import network.holographics.api.utils.scheduler.S;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    private static final HolographicsInternal DH = HolographicsInternalAPI.get();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        S.async(() -> DH.getHologramManager().updateVisibility(player));
        S.sync(() -> DH.getPacketListener().hook(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        S.async(() -> DH.getHologramManager().onQuit(player));
        DH.getPacketListener().unhook(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        S.async(() -> DH.getHologramManager().updateVisibility(player));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        S.async(() -> DH.getHologramManager().updateVisibility(player));
    }

}
