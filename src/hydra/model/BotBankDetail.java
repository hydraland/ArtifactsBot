package hydra.model;

import java.io.Serializable;

public class BotBankDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private int slots;
	private int nextExpansionCost;
	private int gold;
	private int expansions;

	public final int getSlots() {
		return slots;
	}

	public final void setSlots(int slots) {
		this.slots = slots;
	}

	public final int getNextExpansionCost() {
		return nextExpansionCost;
	}

	public final void setNextExpansionCost(int nextExpansionCost) {
		this.nextExpansionCost = nextExpansionCost;
	}

	public final int getGold() {
		return gold;
	}

	public final void setGold(int gold) {
		this.gold = gold;
	}

	public final int getExpansions() {
		return expansions;
	}

	public final void setExpansions(int expansions) {
		this.expansions = expansions;
	}

}
