package network.holographics.plugin;

import com.sun.istack.internal.NotNull;
import network.holographics.api.HolographicsInternal;
import network.holographics.api.HolographicsInternalAPI;
import network.holographics.api.holograms.Hologram;
import network.holographics.api.holograms.HologramLine;
import network.holographics.api.holograms.HologramPage;
import network.holographics.api.holograms.enums.EnumFlag;
import network.holographics.api.utils.Common;
import network.holographics.api.utils.items.HologramItem;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@UtilityClass
public final class Validator {

    private static final HolographicsInternal HOLOGRAPHICS_INTERNAL = HolographicsInternalAPI.get();

    /*
     *	General Methods
     */

    public static Hologram getHologram(String name) throws RuntimeException {
        Hologram hologram = HOLOGRAPHICS_INTERNAL.getHologramManager().getHologram(name);
        if (hologram == null) {
            throw new RuntimeException("Hologram with that name couldn't be found.");
        }
        return hologram;
    }

    public static HologramPage getHologramPage(Hologram hologram, int index) throws RuntimeException {
        index = getIntegerInRange(index, 1, hologram.size(), "Page index must be in bounds of given hologram.");
        return hologram.getPage(index);
    }

    public static HologramLine getHologramLine(HologramPage page, int index, String message) throws RuntimeException {
        HologramLine line = page.getLine(Validator.getHologramLineIndex(page, index) - 1);
        if (line == null) {
            throw new RuntimeException(message);
        }
        return line;
    }

    public static HologramLine getHologramLine(HologramPage page, int index) throws RuntimeException {
        return getHologramLine(page, index, "Hologram line couldn't be found.");
    }

    /*
     *	Hologram Methods
     */

    public static Hologram getHologram(String name, String message) throws RuntimeException {
        Hologram hologram = HOLOGRAPHICS_INTERNAL.getHologramManager().getHologram(name);
        if (hologram == null) throw new RuntimeException(message);
        return hologram;
    }

    public static HologramPage getHologramPage(Hologram hologram, int index, String message) throws RuntimeException {
        index = getIntegerInRange(index, 1, hologram.size(), message);
        return hologram.getPage(index - 1);
    }

    public static HologramPage getHologramPage(Hologram hologram, String indexString, String message) throws RuntimeException {
        int index = getInteger(indexString, 1, hologram.size(), message);
        return hologram.getPage(index - 1);
    }

    public static int getHologramLineIndex(HologramPage page, int index) throws RuntimeException {
        return getIntegerInRange(index, 1, page.size(), "Line index must be in bounds of given hologram page.");
    }

    public static int getHologramLineIndex(HologramPage page, String index) throws RuntimeException {
        return Validator.getInteger(index, 1, page.size(), "Line index must be in bounds of given hologram page.");
    }

    public static EnumFlag getFlag(String string, String message) throws RuntimeException {
        try {
            return EnumFlag.valueOf(string);
        } catch (Exception e) {
            throw new RuntimeException(message);
        }
    }

    /*
     *	String & Arrays Methods
     */

    @NonNull
    public static String getString(String[] arr, int beginIndex, int endIndex) {
        return String.join(" ", Arrays.copyOfRange(arr, beginIndex, endIndex));
    }

    @NonNull
    public static String getLineContent(String @NonNull [] args, int beginIndex) {
        String text = "";
        if (args.length > beginIndex) {
            String[] textArray = Arrays.copyOfRange(args, beginIndex, args.length);
            text = textArray.length == 1 ? textArray[0] : String.join(" ", textArray);
        }
        return text;
    }

    @NonNull
    public static String getLineContent(Player player, String @NonNull [] args, int beginIndex) {
        String text = "";
        if (args.length > beginIndex) {
            String[] textArray = Arrays.copyOfRange(args, beginIndex, args.length);
            text = textArray.length == 1 ? textArray[0] : String.join(" ", textArray);
            if (text.contains("<HAND>")) {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                    text = text.replace("<HAND>", HologramItem.fromItemStack(itemStack).getContent());
                } else {
                    text = text.replace("<HAND>", "STONE");
                }
            }
        }
        return text;
    }

    /*
     *	CommandSender-Player Methods
     */

    public static Player getPlayer(CommandSender sender) throws RuntimeException {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new RuntimeException("ONLY_PLAYER");
        }
    }

    public static boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    /*
     *	String-Integer Methods
     */

    public static int getIntegerInRange(int i, int min, int max, String message) throws RuntimeException {
        if (i < min || i > max) throw new RuntimeException(Common.PREFIX + "&c" + message);
        return i;
    }

    public static int getInteger(String string, String message) throws RuntimeException {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(message);
        }
    }

    public static int getInteger(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static int getInteger(String string, int min, int max, String message) throws RuntimeException {
        try {
            return getIntegerInRange(Integer.parseInt(string), min, max, message);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(message);
        }
    }

    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /*
     *	String-Float Methods
     */

    public static float getFloat(String string, double min, double max, String message) throws RuntimeException {
        float d = getFloat(string, message);
        if (d < min || d > max) {
            throw new RuntimeException(message);
        }
        return d;
    }

    public static float getFloat(String string, String message) throws RuntimeException {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(message);
        }
    }

    public static boolean isFloat(String string) {
        try {
            Float.parseFloat(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /*
     *	String-Double Methods
     */

    public static double getDouble(String string, double min, double max, String message) throws RuntimeException {
        double d = getDouble(string, message);
        if (d < min || d > max) {
            throw new RuntimeException(message);
        }
        return d;
    }

    public static double getDouble(String string, String message) throws RuntimeException {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(message);
        }
    }

    public static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /*
     *	String-Byte Methods
     */

    public static double getByte(String string, byte min, byte max, String message) throws RuntimeException {
        double d = getByte(string, message);
        if (d < min || d > max) {
            throw new RuntimeException(message);
        }
        return d;
    }

    public static double getByte(String string, String message) throws RuntimeException {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(message);
        }
    }

    public static boolean isByte(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /*
     *	String-Boolean Methods
     */

    public static boolean getBoolean(String string, String message) throws RuntimeException {
        try {
            return Boolean.parseBoolean(string);
        } catch (Exception e) {
            throw new RuntimeException(message);
        }
    }

    public static boolean isBoolean(String string) {
        try {
            Boolean.parseBoolean(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static double getDouble(String string) {
        try {
            return Double.parseDouble(string);
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static double getLocationValue(@NotNull String string, double initialValue) {
        boolean isDiff = false;
        if (string.startsWith("~")) {
            isDiff = true;
            string = string.substring(1);
        }

        double number = getDouble(string);
        if (isDiff) {
            return initialValue + number;
        }
        return number;
    }

}
