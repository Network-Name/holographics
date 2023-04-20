package network.holographics.api.holograms.objects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public abstract class UpdatingHologramObject extends HologramObject {

    /*
     *	Fields
     */

    public int displayRange = 48;
    public int updateRange = 48;
    public volatile int updateInterval = 20;

    /*
     *	Constructors
     */

    public UpdatingHologramObject(@NonNull Location location) {
        super(location);
    }

}
