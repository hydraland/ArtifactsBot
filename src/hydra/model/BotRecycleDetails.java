package hydra.model;

import java.io.Serializable;
import java.util.List;

public final class BotRecycleDetails implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<BotItem> items;

	public List<BotItem> getItems() {
		return items;
	}

	public void setItems(List<BotItem> items) {
		this.items = items;
	}

}
