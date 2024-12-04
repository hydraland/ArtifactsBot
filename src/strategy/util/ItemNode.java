package strategy.util;

import java.util.ArrayList;
import java.util.List;

import hydra.model.BotItemDetails;

public final class ItemNode {

	private BotItemDetails botItemDetails;
	private List<ItemTransition> children;

	public void setValue(BotItemDetails botItemDetails) {
		this.botItemDetails = botItemDetails;
	}

	public void addTransition(String code, int quantity) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(new ItemTransition(code, quantity));
	}

	public BotItemDetails getValue() {
		return botItemDetails;
	}

	public List<ItemTransition> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "ItemNode [botItemDetails=" + botItemDetails + ", children=" + children + "]";
	}
}