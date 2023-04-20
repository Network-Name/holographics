package network.holographics.api.nms;

import io.netty.channel.ChannelPipeline;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketListener {

    private static final NMS nms = NMS.getInstance();
    private static final String IDENTIFIER = "Holographics";
    private boolean usingProtocolLib = false;

    public PacketListener() {
        hookAll();
    }

    public void destroy() {
        unhookAll();
    }

    public boolean hook(Player player) {
        try {
            ChannelPipeline pipeline = nms.getPipeline(player);
            if (pipeline.get(IDENTIFIER) == null) {
                PacketHandlerCustom packetHandler = new PacketHandlerCustom(player);
                pipeline.addBefore("packet_handler", IDENTIFIER, packetHandler);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void hookAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hook(player);
        }
    }

    public boolean unhook(Player player) {
        try {
            ChannelPipeline pipeline = NMS.getInstance().getPipeline(player);
            if (pipeline.get(IDENTIFIER) != null) {
                pipeline.remove(IDENTIFIER);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unhookAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            unhook(player);
        }
    }

}
