package network.holographics.api.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import network.holographics.api.HolographicsInternal;
import network.holographics.api.HolographicsInternalAPI;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;

@UtilityClass
public class BungeeUtils {

    private static final HolographicsInternal HOLOGRAPHICS_INTERNAL = HolographicsInternalAPI.get();
    private static final String BUNGEE_CORD_CHANNEL = "BungeeCord";
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        Messenger messenger = Bukkit.getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(HOLOGRAPHICS_INTERNAL.getPlugin(), BUNGEE_CORD_CHANNEL);
        initialized = true;
    }

    public static void destroy() {
        if (!initialized) return;
        Messenger messenger = Bukkit.getServer().getMessenger();
        messenger.unregisterOutgoingPluginChannel(HOLOGRAPHICS_INTERNAL.getPlugin(), BUNGEE_CORD_CHANNEL);
        initialized = false;
    }

    public static void connect(Player player, String server) {
        if (!initialized) init();
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(HOLOGRAPHICS_INTERNAL.getPlugin(), BUNGEE_CORD_CHANNEL, out.toByteArray());
        } catch (Exception ignored) {
            // Ignore
        }
    }

}
