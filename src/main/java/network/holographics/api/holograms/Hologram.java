package network.holographics.api.holograms;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import network.holographics.api.HolographicsInternal;
import network.holographics.api.HolographicsInternalAPI;
import network.holographics.api.actions.ClickType;
import network.holographics.api.holograms.enums.EnumFlag;
import network.holographics.api.holograms.objects.UpdatingHologramObject;
import network.holographics.api.nms.NMS;
import network.holographics.api.utils.collection.DList;
import network.holographics.api.utils.event.EventFactory;
import network.holographics.api.utils.reflect.Version;
import network.holographics.api.utils.scheduler.S;
import network.holographics.api.utils.tick.ITicked;
import network.holographics.event.HologramClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
public class Hologram extends UpdatingHologramObject implements ITicked {

    private static final HolographicsInternal HOLOGRAPHICS_INTERNAL = HolographicsInternalAPI.get();

    /*
     *	Hologram Cache
     */

    /**
     * This map contains all cached holograms. This map is used to get holograms by name.
     * <p>
     * Holograms are cached when they are loaded from files or created. They are removed
     * from the cache when they are deleted.
     * <p>
     * Holograms, that are only in this map and not in the {@link HologramManager}, are not
     * editable via commands. They are only editable via the API.
     *
     * @see #getCachedHologram(String)
     */
    private static final @NonNull Map<String, Hologram> CACHED_HOLOGRAMS;

    static {
        CACHED_HOLOGRAMS = new ConcurrentHashMap<>();
    }

    public static Hologram getCachedHologram(@NonNull String name) {
        return CACHED_HOLOGRAMS.get(name);
    }

    @NonNull
    @Contract(pure = true)
    public static Set<String> getCachedHologramNames() {
        return CACHED_HOLOGRAMS.keySet();
    }

    @NonNull
    @Contract(pure = true)
    public static Collection<Hologram> getCachedHolograms() {
        return CACHED_HOLOGRAMS.values();
    }

    /*
     *	Static Methods
     */
    /*
     *	Fields
     */

    /**
     * The lock used to synchronize the saving process of this hologram.
     *
     * @implNote This lock is used to prevent multiple threads from saving
     * the same hologram at the same time. This is important because the
     * saving process is not thread-safe in SnakeYAML.
     * @since 2.7.10
     */
    public final Lock lock = new ReentrantLock();

    /**
     * This object server as a mutex for all visibility related operations.
     * <p>
     * For example, when we want to hide a hologram, that's already being
     * updated on another thread, we would need to wait for the update to
     * finish before we can hide the hologram. That is because if we didn't,
     * parts of the hologram might still be visible after the hide operation,
     * due to the update process.
     *
     * @implNote This lock is used to prevent multiple threads from modifying
     * the visibility of the same hologram at the same time. This is important
     * because the visibility of a hologram is not thread-safe.
     * @since 2.7.11
     */
    public final Object visibilityMutex = new Object();

    public final @NonNull String name;
    public boolean saveToFile;
    public final @NonNull Map<UUID, Integer> viewerPages = new ConcurrentHashMap<>();
    public final @NonNull Set<UUID> hidePlayers = ConcurrentHashMap.newKeySet();
    public final @NonNull Set<UUID> showPlayers = ConcurrentHashMap.newKeySet();
    public boolean defaultVisibleState = true;
    public final @NonNull DList<HologramPage> pages = new DList<>();
    public boolean downOrigin = false;
    public boolean alwaysFacePlayer = false;
    private final @NonNull AtomicInteger tickCounter;

    /*
     *	Constructors
     */

    /**
     * Creates a new hologram with the given name and location. The hologram will be saved to a file.
     *
     * @param name     The name of the hologram.
     * @param location The location of the hologram.
     */


    /**
     * Creates a new hologram with the given name and location. The hologram will be saved to the given file.
     *
     * @param name     The name of the hologram.
     * @param location The location of the hologram.
     */
    public Hologram(@NonNull String name, @NonNull Location location) {
        this(name, location, true);
    }

    /**
     * Creates a new hologram with the given name and location.
     *
     * @param name     The name of the hologram.
     * @param location The location of the hologram.
     * @param enabled  Whether the hologram should be enabled.
     */
    public Hologram(@NonNull String name, @NonNull Location location, boolean enabled) {
        super(location);
        this.name = name;
        this.enabled = enabled;
        this.saveToFile = false;
        this.tickCounter = new AtomicInteger();
        this.addPage();
        this.register();

        CACHED_HOLOGRAMS.put(this.name, this);
    }

    /*
     *	Tick
     */

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public long getInterval() {
        return 1L;
    }

    @Override
    public void tick() {
        if (tickCounter.get() == getUpdateInterval()) {
            tickCounter.set(1);
            updateAll();
            return;
        }
        tickCounter.incrementAndGet();
        updateAnimationsAll();
    }

    /*
     *	General Methods
     */

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "name=" + getName() +
                ", enabled=" + isEnabled() +
                "} " + super.toString();
    }

    /**
     * This method calls {@link #destroy()} before deleting the holograms file.
     */
    @Override
    public void delete() {
        super.delete();
    }

    /**
     * This method disables the hologram, removes it from the {@link HologramManager},
     * removes it from the cache and hides it from all players.
     */
    @Override
    public void destroy() {
        this.disable(DisableCause.API);
        this.viewerPages.clear();
        HOLOGRAPHICS_INTERNAL.getHologramManager().removeHologram(getName());
        CACHED_HOLOGRAMS.remove(getName());
    }

    /**
     * This method enables the hologram, calls the {@link #register()} method
     * to start the update task and shows it to all players.
     */
    @Override
    public void enable() {
        synchronized (visibilityMutex) {
            super.enable();
            this.showAll();
            this.register();
        }
    }

    /**
     * This method disables the hologram, calls the {@link #unregister()} method
     * to stop the update task and hides it from all players.
     */
    @Override
    public void disable(@NonNull DisableCause cause) {
        synchronized (visibilityMutex) {
            this.unregister();
            this.hideAll();
            super.disable(cause);
        }
    }

    @Override
    public void setFacing(float facing) {
        final float prev = this.facing;

        super.setFacing(facing);

        // Update the facing for all lines, that don't already have a different facing set.
        // We want to keep the hologram facing working as a "default" value, but we don't want
        // it to override custom line facing.
        for (HologramPage page : this.pages) {
            page.getLines().forEach(line -> {
                if (line.getFacing() == prev) {
                    line.setFacing(facing);
                }
                page.realignLines();
            });
        }
    }

    /**
     * Set the location of this hologram. This method doesn't update the holograms location
     * for the players, you have to call {@link #realignLines()} for that.
     *
     * @param location The new location of this hologram.
     */
    @Override
    public void setLocation(@NonNull Location location) {
        super.setLocation(location);
        teleportClickableEntitiesAll();
    }

    /**
     * Get hologram size. (Number of pages)
     *
     * @return Number of pages in this hologram.
     */
    public int size() {
        return pages.size();
    }

    /**
     * Save this hologram to a file asynchronously.
     *
     * @implNote Always returns true. If the hologram is not persistent,
     * this method just doesn't do anything.
     */

    /**
     * Create a new instance of this hologram object that's identical to this one.
     *
     * @param name     Name of the clone.
     * @param location Location of the clone.
     * @param temp     True if the clone should only exist until the next reload. (Won't save to file)
     * @return Cloned instance of this line.
     */
    public Hologram clone(@NonNull String name, @NonNull Location location, boolean temp) {
        Hologram hologram = new Hologram(name, location.clone(), !temp);
        hologram.setDownOrigin(this.isDownOrigin());
        hologram.setPermission(this.getPermission());
        hologram.setFacing(this.getFacing());
        hologram.setDisplayRange(this.getDisplayRange());
        hologram.setUpdateRange(this.getUpdateRange());
        hologram.setUpdateInterval(this.getUpdateInterval());
        hologram.addFlags(this.getFlags().toArray(new EnumFlag[0]));
        hologram.setDefaultVisibleState(this.isDefaultVisibleState());
        hologram.showPlayers.addAll(this.showPlayers);
        hologram.hidePlayers.addAll(this.hidePlayers);

        for (int i = 0; i < size(); i++) {
            HologramPage page = getPage(i);
            HologramPage clonePage = page.clone(hologram, i);
            if (hologram.pages.size() > i) {
                hologram.pages.set(i, clonePage);
            } else {
                hologram.pages.add(clonePage);
            }
        }
        return hologram;
    }

    /**
     * Handle a click on this hologram.
     *
     * @param player    The player that clicked the hologram.
     * @param entityId  The id of the clicked entity.
     * @param clickType The type of the click.
     * @return True if the click was handled, false otherwise.
     */
    public boolean onClick(@NonNull Player player, int entityId, @NonNull ClickType clickType) {
        if (this.hasFlag(EnumFlag.DISABLE_ACTIONS)) {
            return false;
        }
        HologramPage page = getPage(player);
        boolean clickedThisHologram = page != null && page.hasEntity(entityId);
        boolean eventNotCancelled = EventFactory.handleHologramClickEvent(player, this, page, clickType, entityId);
        if (clickedThisHologram && eventNotCancelled) {
            page.executeActions(player, clickType);
            return true;
        }
        return false;
    }

    /**
     * Handle the player quit event for this hologram. This method will hide the hologram
     * from the player and remove the player from the show/hide lists.
     *
     * @param player The player that quit.
     */
    public void onQuit(@NonNull Player player) {
        hide(player);
        removeShowPlayer(player);
        removeHidePlayer(player);
        viewerPages.remove(player.getUniqueId());
    }

    /*
     *	Visibility Methods
     */

    /**
     * Set default display state
     *
     * @param state state
     */
    public void setDefaultVisibleState(boolean state) {
        this.defaultVisibleState = state;
    }

    /**
     * @return Default display state
     */
    public boolean isVisibleState() {
        return defaultVisibleState;
    }

    /**
     * Set player hide state
     *
     * @param player player
     */
    public void setHidePlayer(@NonNull Player player) {
        UUID uniqueId = player.getUniqueId();
        if (!hidePlayers.contains(uniqueId)) {
            hidePlayers.add(player.getUniqueId());
        }
    }

    /**
     * Remove a player hide state
     *
     * @param player player
     */
    public void removeHidePlayer(@NonNull Player player) {
        UUID uniqueId = player.getUniqueId();
        hidePlayers.remove(uniqueId);
    }

    /**
     * Determine if the player can't see the hologram
     *
     * @param player player
     * @return state
     */
    public boolean isHideState(@NonNull Player player) {
        return hidePlayers.contains(player.getUniqueId());
    }

    /**
     * Set player show state
     *
     * @param player player
     */
    public void setShowPlayer(@NonNull Player player) {
        UUID uniqueId = player.getUniqueId();
        if (!showPlayers.contains(uniqueId)) {
            showPlayers.add(player.getUniqueId());
        }
    }

    /**
     * Remove a player show state
     *
     * @param player player
     */
    public void removeShowPlayer(@NonNull Player player) {
        UUID uniqueId = player.getUniqueId();
        showPlayers.remove(uniqueId);
    }

    /**
     * Determine if the player can see the hologram
     *
     * @param player player
     * @return state
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isShowState(@NonNull Player player) {
        return showPlayers.contains(player.getUniqueId());
    }

    /**
     * Show this hologram for given player on a given page.
     *
     * @param player    Given player.
     * @param pageIndex Given page.
     */
    public boolean show(@NonNull Player player, int pageIndex) {
        synchronized (visibilityMutex) {
            if (isDisabled() || isHideState(player) || (!isDefaultVisibleState() && !isShowState(player))) {
                return false;
            }
            HologramPage page = getPage(pageIndex);
            if (page != null && page.size() > 0 && canShow(player) && isInDisplayRange(player)) {
                if (isVisible(player)) {
                    hide(player);
                }
                if (Version.after(8)) {
                    showPageTo(player, page, pageIndex);
                } else {
                    // We need to run the task later on older versions as, if we don't, it causes issues with some holograms *randomly* becoming invisible.
                    // I *think* this is from despawning and spawning the entities (with the same ID) in the same tick.
                    S.sync(() -> showPageTo(player, page, pageIndex), 0L);
                }
                return true;
            }
            return false;
        }
    }

    private void showPageTo(@NonNull Player player, @NonNull HologramPage page, int pageIndex) {
        page.getLines().forEach(line -> line.show(player));
        // Add player to viewers
        viewerPages.put(player.getUniqueId(), pageIndex);
        viewers.add(player.getUniqueId());
        showClickableEntities(player);
    }

    public void showAll() {
        synchronized (visibilityMutex) {
            if (isEnabled()) {
                Bukkit.getOnlinePlayers().forEach(player -> show(player, getPlayerPage(player)));
            }
        }
    }

    public void update(@NonNull Player player) {
        synchronized (visibilityMutex) {
            if (hasFlag(EnumFlag.DISABLE_UPDATING) || !isVisible(player) || !isInUpdateRange(player) || isHideState(player)) {
                return;
            }

            HologramPage page = getPage(player);
            if (page != null) {
                page.getLines().forEach(line -> line.update(player));
            }
        }
    }

    public void updateAll() {
        synchronized (visibilityMutex) {
            if (isEnabled() && !hasFlag(EnumFlag.DISABLE_UPDATING)) {
                getViewerPlayers().forEach(this::update);
            }
        }
    }

    public void updateAnimations(@NonNull Player player) {
        synchronized (visibilityMutex) {
            if (hasFlag(EnumFlag.DISABLE_ANIMATIONS) || !isVisible(player) || !isInUpdateRange(player) || isHideState(player)) {
                return;
            }

            HologramPage page = getPage(player);
            if (page != null) {
                page.getLines().forEach(line -> line.updateAnimations(player));
            }
        }
    }

    public void updateAnimationsAll() {
        synchronized (visibilityMutex) {
            if (isEnabled() && !hasFlag(EnumFlag.DISABLE_ANIMATIONS)) {
                getViewerPlayers().forEach(this::updateAnimations);
            }
        }
    }

    public void hide(@NonNull Player player) {
        synchronized (visibilityMutex) {
            if (isVisible(player)) {
                HologramPage page = getPage(player);
                if (page != null) {
                    page.getLines().forEach(line -> line.hide(player));
                    hideClickableEntities(player);
                }
                viewers.remove(player.getUniqueId());
            }
        }
    }

    public void hideAll() {
        synchronized (visibilityMutex) {
            if (isEnabled()) {
                getViewerPlayers().forEach(this::hide);
            }
        }
    }

    public void showClickableEntities(@NonNull Player player) {
        HologramPage page = getPage(player);
        if (page == null || !(page.isClickable() || HologramClickEvent.isRegistered())) {
            return;
        }

        // Spawn clickable entities
        NMS nms = NMS.getInstance();
        int amount = (int) (page.getHeight() / 2) + 1;
        Location location = getLocation().clone();
        location.setY((int) (location.getY() - (isDownOrigin() ? 0 : page.getHeight())) + 0.5);
        for (int i = 0; i < amount; i++) {
            int id = page.getClickableEntityId(i);
            nms.showFakeEntityArmorStand(player, location, id, true, false, true);
            location.add(0, 1.8, 0);
        }
    }

    public void showClickableEntitiesAll() {
        if (isEnabled()) {
            getViewerPlayers().forEach(this::showClickableEntities);
        }
    }

    public void hideClickableEntities(@NonNull Player player) {
        HologramPage page = getPage(player);
        if (page == null) {
            return;
        }

        // Despawn clickable entities
        NMS nms = NMS.getInstance();
        page.getClickableEntityIds().forEach(id -> nms.hideFakeEntities(player, id));
    }

    public void hideClickableEntitiesAll() {
        if (isEnabled()) {
            getViewerPlayers().forEach(this::hideClickableEntities);
        }
    }

    public void teleportClickableEntities(@NonNull Player player) {
        HologramPage page = getPage(player);
        if (page == null || !(page.isClickable() || HologramClickEvent.isRegistered())) {
            return;
        }

        // Spawn clickable entities
        NMS nms = NMS.getInstance();
        int amount = (int) (page.getHeight() / 2) + 1;
        Location location = getLocation().clone();
        location.setY((int) (location.getY() - (isDownOrigin() ? 0 : page.getHeight())) + 0.5);
        for (int i = 0; i < amount; i++) {
            int id = page.getClickableEntityId(i);
            nms.teleportFakeEntity(player, location, id);
            location.add(0, 1.8, 0);
        }
    }

    public void teleportClickableEntitiesAll() {
        if (isEnabled()) {
            getViewerPlayers().forEach(this::teleportClickableEntities);
        }
    }


    /**
     * Check whether the given player is in display range of this hologram object.
     *
     * @param player Given player.
     * @return Boolean whether the given player is in display range of this hologram object.
     */
    public boolean isInDisplayRange(@NonNull Player player) {
        /*
         * Some forks (e.g. Pufferfish) throw an exception, when we try to get
         * the world of a location, which is not loaded. We catch this exception
         * and return false, because the player is not in range.
         */
        try {
            if (player.getWorld().equals(location.getWorld())) {
                return player.getLocation().distanceSquared(location) <= displayRange * displayRange;
            }
        } catch (Exception ignored) {
            // Ignored
        }
        return false;
    }

    /**
     * Check whether the given player is in update range of this hologram object.
     *
     * @param player Given player.
     * @return Boolean whether the given player is in update range of this hologram object.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isInUpdateRange(@NonNull Player player) {
        /*
         * Some forks (e.g. Pufferfish) throw an exception, when we try to get
         * the world of a location, which is not loaded. We catch this exception
         * and return false, because the player is not in range.
         */
        try {
            if (player.getWorld().equals(location.getWorld())) {
                return player.getLocation().distanceSquared(location) <= updateRange * updateRange;
            }
        } catch (Exception ignored) {
            // Ignored
        }
        return false;
    }

    public void setDownOrigin(boolean downOrigin) {
        this.downOrigin = downOrigin;
        this.hideClickableEntitiesAll();
        this.showClickableEntitiesAll();
    }

    /*
     *	Viewer Methods
     */

    public int getPlayerPage(@NonNull Player player) {
        return viewerPages.getOrDefault(player.getUniqueId(), 0);
    }

    public Set<Player> getViewerPlayers(int pageIndex) {
        Set<Player> players = new HashSet<>();
        viewerPages.forEach((uuid, integer) -> {
            if (integer == pageIndex) {
                players.add(Bukkit.getPlayer(uuid));
            }
        });
        return players;
    }

    /*
     *	Pages Methods
     */

    /**
     * Re-Align the lines in this hologram putting them to the right place.
     * <p>
     * This method is good to use after teleporting the hologram.
     * </p>
     */
    public void realignLines() {
        for (HologramPage page : pages) {
            page.realignLines();
        }
    }

    public HologramPage addPage() {
        HologramPage page = new HologramPage(this, pages.size());
        pages.add(page);
        return page;
    }

    public HologramPage insertPage(int index) {
        if (index < 0 || index > size()) return null;
        HologramPage page = new HologramPage(this, index);
        pages.add(index, page);

        // Add 1 to indexes of all the other pages.
        pages.stream().skip(index).forEach(p -> p.setIndex(p.getIndex() + 1));
        // Add 1 to all page indexes of current viewers, so they still see the same page.
        viewerPages.replaceAll((uuid, integer) -> {
            if (integer > index) {
                return integer + 1;
            }
            return integer;
        });
        return page;
    }

    public HologramPage getPage(int index) {
        if (index < 0 || index >= size()) return null;
        return pages.get(index);
    }

    public HologramPage getPage(@NonNull Player player) {
        if (isVisible(player)) {
            return getPage(getPlayerPage(player));
        }
        return null;
    }

    public HologramPage removePage(int index) {
        if (index < 0 || index > size()) {
            return null;
        }

        HologramPage page = pages.remove(index);
        page.getLines().forEach(HologramLine::hide);

        // Update indexes of all the other pages.
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).setIndex(i);
        }

        // Update all page indexes of current viewers, so they still see the same page.
        if (pages.isNotEmpty()) {
            for (Map.Entry<UUID, Integer> entry : viewerPages.entrySet()) {
                UUID uuid = entry.getKey();
                int currentPage = viewerPages.get(uuid);
                if (currentPage == index) {
                    show(Bukkit.getPlayer(uuid), 0);
                } else if (currentPage > index) {
                    viewerPages.put(uuid, currentPage - 1);
                }
            }
        }
        return page;
    }

    public boolean swapPages(int index1, int index2) {
        if (index1 == index2 || index1 < 0 || index1 >= size() || index2 < 0 || index2 >= size()) {
            return false;
        }
        // Swap them in the list
        Collections.swap(pages, index1, index2);

        // Swap indexes of affected pages
        HologramPage page1 = getPage(index1);
        HologramPage page2 = getPage(index2);
        int i = page1.getIndex();
        page1.setIndex(page2.getIndex());
        page2.setIndex(i);

        // Swap viewers
        Set<Player> viewers1 = getViewerPlayers(index1);
        Set<Player> viewers2 = getViewerPlayers(index2);
        viewers1.forEach(player -> show(player, index2));
        viewers2.forEach(player -> show(player, index1));
        return true;
    }

    /**
     * Get the list of all pages in this hologram.
     *
     * @return List of all pages in this hologram.
     */
    public List<HologramPage> getPages() {
        return ImmutableList.copyOf(pages);
    }

}
