package hydra.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BotGatheringDetails implements Serializable {
	private static final long serialVersionUID = 1L;
	private int xp;
	private List<BotItem> items;

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

	public List<BotItem> getItems() {
		return items;
	}

	public void setItems(List<BotItem> items) {
		this.items = items;
	}
	
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("xp", xp);
		builder.append("items", items);
		return builder.toString();
	}
}
