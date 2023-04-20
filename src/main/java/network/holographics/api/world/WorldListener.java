package network.holographics.api.world;

import network.holographics.api.HolographicsInternal;
import network.holographics.api.HolographicsInternalAPI;
import network.holographics.api.holograms.DisableCause;
import network.holographics.api.holograms.Hologram;
import network.holographics.api.holograms.HologramManager;
import network.holographics.api.utils.scheduler.S;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {

    private static final HolographicsInternal DH = HolographicsInternalAPI.get();

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        HologramManager hm = DH.getHologramManager();
        World world = event.getWorld();

        S.async(() -> hm.getHolograms().stream()
                .filter(Hologram::isEnabled)
                .filter(hologram -> hologram.getLocation().getWorld().equals(world))
                .forEach(hologram -> hologram.disable(DisableCause.WORLD_UNLOAD)));
    }
}
