package hydra.model;

import java.io.Serializable;
import java.util.List;

public class BotCraft implements Serializable {
	private static final long serialVersionUID = 1L;

	private int level;
	private int quantity;
	private BotCraftSkill skill;
	private List<BotItem> items;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BotCraftSkill getSkill() {
		return skill;
	}

	public void setSkill(BotCraftSkill skill) {
		this.skill = skill;
	}

	public List<BotItem> getItems() {
		return items;
	}

	public void setItems(List<BotItem> items) {
		this.items = items;
	}

}
