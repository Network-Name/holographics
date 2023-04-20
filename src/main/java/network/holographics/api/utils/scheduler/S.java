package network.holographics.api.utils.scheduler;

import network.holographics.api.HolographicsInternal;
import network.holographics.api.HolographicsInternalAPI;
import network.holographics.api.utils.DExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitTask;

public class S {

    private static final HolographicsInternal HOLOGRAPHICS_INTERNAL = HolographicsInternalAPI.get();

    public static void stopTask(int id) {
        Bukkit.getScheduler().cancelTask(id);
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(HOLOGRAPHICS_INTERNAL.getPlugin(), runnable);
    }

    public static BukkitTask sync(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(HOLOGRAPHICS_INTERNAL.getPlugin(), runnable, delay);
    }

    public static BukkitTask syncTask(Runnable runnable, long interval) {
        return Bukkit.getScheduler().runTaskTimer(HOLOGRAPHICS_INTERNAL.getPlugin(), runnable, 0, interval);
    }

    public static void async(Runnable runnable) {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(HOLOGRAPHICS_INTERNAL.getPlugin(), runnable);
        } catch (IllegalPluginAccessException e) {
            DExecutor.execute(runnable);
        }
    }

    public static void async(Runnable runnable, long delay) {
        try {
            Bukkit.getScheduler().runTaskLaterAsynchronously(HOLOGRAPHICS_INTERNAL.getPlugin(), runnable, delay);
        } catch (IllegalPluginAccessException e) {
            DExecutor.execute(runnable);
        }
    }

    public static BukkitTask asyncTask(Runnable runnable, long interval) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(HOLOGRAPHICS_INTERNAL.getPlugin(), runnable, 0, interval);
    }

    public static BukkitTask asyncTask(Runnable runnable, long interval, long delay) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(HOLOGRAPHICS_INTERNAL.getPlugin(), runnable, delay, interval);
    }

}
