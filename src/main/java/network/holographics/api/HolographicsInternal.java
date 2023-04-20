package network.holographics.api;

import network.holographics.api.animations.AnimationManager;
import network.holographics.api.features.FeatureManager;
import network.holographics.api.holograms.Hologram;
import network.holographics.api.holograms.HologramManager;
import network.holographics.api.nms.NMS;
import network.holographics.api.nms.PacketListener;
import network.holographics.api.player.PlayerListener;
import network.holographics.api.utils.BungeeUtils;
import network.holographics.api.utils.Common;
import network.holographics.api.utils.DExecutor;
import network.holographics.api.utils.event.EventFactory;
import network.holographics.api.utils.reflect.ReflectionUtil;
import network.holographics.api.utils.reflect.Version;
import network.holographics.api.utils.tick.Ticker;
import network.holographics.api.world.WorldListener;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

@Getter
public final class HolographicsInternal {

    private final JavaPlugin plugin;
    private HologramManager hologramManager;
    private FeatureManager featureManager;
    private AnimationManager animationManager;
    private PacketListener packetListener;
    private Ticker ticker;
    private File dataFolder;
    private boolean updateAvailable;

    /*
     *	Constructors
     */

    public HolographicsInternal(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     *	General Methods
     */

    public void load() {
        // Check if NMS version is supported
        if (Version.CURRENT == null) {
            Common.log(Level.SEVERE, "Unsupported server version: " + ReflectionUtil.getVersion());
            Common.log(Level.SEVERE, "Plugin will be disabled.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void enable() {
        NMS.init();
        DExecutor.init(3);

        this.ticker = new Ticker();
        this.hologramManager = new HologramManager();
        this.featureManager = new FeatureManager();
        this.animationManager = new AnimationManager();
        this.packetListener = new PacketListener();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(), this.plugin);
        pm.registerEvents(new WorldListener(), this.plugin);

        BungeeUtils.init();
    }

    public void disable() {
        this.packetListener.destroy();
        this.featureManager.destroy();
        this.hologramManager.destroy();
        this.animationManager.destroy();
        this.ticker.destroy();

        for (Hologram hologram : Hologram.getCachedHolograms()) {
            hologram.destroy();
        }

        BungeeUtils.destroy();
        DExecutor.shutdownNow();
    }

    public void reload() {
        this.animationManager.reload();
        this.hologramManager.reload();
        this.featureManager.reload();

        EventFactory.handleReloadEvent();
    }

    public File getDataFolder() {
        if (this.dataFolder == null) {
            this.dataFolder = new File("plugins/BedWars");
            this.dataFolder = new File("plugins/BedWars/holograms");
        }
        return this.dataFolder;
    }
}
