package network.holographics.api;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public final class HolographicsInternalAPI {

    private static HolographicsInternal implementation;
    public static boolean enabled = false;

    public static void onLoad(@NonNull JavaPlugin plugin) {
        if (implementation != null) {
            return;
        }
        implementation = new HolographicsInternal(plugin);
        implementation.load();
    }
    
    public static void onEnable() {
        if (implementation == null) {
            return;
        }
        enabled = true;
        implementation.enable();
    }
    
    public static void onDisable() {
        if (implementation == null) {
            return;
        }
        implementation.disable();
        implementation = null;
        enabled = false;
    }
    public static boolean isRunning() {
        return implementation != null && enabled;
    }

    public static HolographicsInternal get() {
        if (implementation == null || !enabled) {
            throw new IllegalStateException("Holographics is not running (yet). Do you have Holographics plugin installed?");
        }
        return implementation;
    }

}
