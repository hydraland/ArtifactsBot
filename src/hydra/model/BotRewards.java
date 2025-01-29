package hydra.model;

import java.io.Serializable;
import java.util.List;

public final class BotRewards implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<BotItem> items;
	private int gold;

	public final List<BotItem> getItems() {
		return items;
	}

	public final void setItems(List<BotItem> items) {
		this.items = items;
	}

	public final int getGold() {
		return gold;
	}

	public final void setGold(int gold) {
		this.gold = gold;
	}
}
