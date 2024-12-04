package strategy.achiever;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class GoalParameter {

	private int minFreeSlot;
	private int rareItemSeuil;
	private int coinReserve;
	private int minFreeInventorySpace;
	private boolean optimizeItemTask;

	public GoalParameter(int minFreeSlot, int rareItemSeuil, int coinReserve, int minFreeInventorySpace, boolean optimizeItemTask) {
		this.minFreeSlot = minFreeSlot;
		this.rareItemSeuil = rareItemSeuil;
		this.coinReserve = coinReserve;
		this.minFreeInventorySpace = minFreeInventorySpace;
		this.optimizeItemTask = optimizeItemTask;
	}

	public void setMinFreeSlot(int minFreeSlot) {
		this.minFreeSlot = minFreeSlot;
	}

	public int getMinFreeSlot() {
		return minFreeSlot;
	}

	public final int getRareItemSeuil() {
		return rareItemSeuil;
	}

	public final void setRareItemSeuil(int rareItemSeuil) {
		this.rareItemSeuil = rareItemSeuil;
	}

	public int getCoinReserve() {
		return coinReserve;
	}
	
	public final void setCoinReserve(int coinReserve) {
		this.coinReserve = coinReserve;
	}

	public int getMinFreeInventorySpace() {
		return minFreeInventorySpace;
	}

	public void setMinFreeInventorySpace(int minFreeInventorySpace) {
		this.minFreeInventorySpace = minFreeInventorySpace;
	}

	public final boolean isOptimizeItemTask() {
		return optimizeItemTask;
	}

	public final void setOptimizeItemTask(boolean optimizeItemTask) {
		this.optimizeItemTask = optimizeItemTask;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("minFreeSlot", minFreeSlot);
		builder.append("rareItemSeuil", rareItemSeuil);
		builder.append("coinReserve", coinReserve);
		builder.append("minFreeInventorySpace", minFreeInventorySpace);
		builder.append("optimizeItemTask", optimizeItemTask);
		return builder.toString();
	}
}
