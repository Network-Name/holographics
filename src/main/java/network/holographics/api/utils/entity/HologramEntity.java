package network.holographics.api.utils.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.EntityType;

@Getter
@AllArgsConstructor
public class HologramEntity {

	private final String content;
	private EntityType type;

	public HologramEntity(String string) {
		this.content = string;
		this.type = HolographicsEntityType.parseEntityType(content.trim());
		if (this.type == null) {
			this.type = EntityType.PIG;
		}
	}

}
