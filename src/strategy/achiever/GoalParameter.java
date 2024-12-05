package strategy.achiever;

import org.apache.commons.lang3.builder.ToStringBuilder;

import strategy.achiever.factory.ItemTaskFactory;
import strategy.achiever.factory.MonsterTaskFactory;
import strategy.util.fight.factory.HPRecoveryFactory;

public class GoalParameter {

	private int minFreeSlot;
	private int rareItemSeuil;
	private int coinReserve;
	private int minFreeInventorySpace;
	private MonsterTaskFactory monsterTaskFactory;
	private ItemTaskFactory itemTaskFactory;
	private HPRecoveryFactory hPRecoveryFactory;

	public GoalParameter(int minFreeSlot, int rareItemSeuil, int coinReserve, int minFreeInventorySpace) {
		this.minFreeSlot = minFreeSlot;
		this.rareItemSeuil = rareItemSeuil;
		this.coinReserve = coinReserve;
		this.minFreeInventorySpace = minFreeInventorySpace;
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
	
	public final MonsterTaskFactory getMonsterTaskFactory() {
		return monsterTaskFactory;
	}

	public final void setMonsterTaskFactory(MonsterTaskFactory monsterTaskFactory) {
		this.monsterTaskFactory = monsterTaskFactory;
	}
	
	public final ItemTaskFactory getItemTaskFactory() {
		return itemTaskFactory;
	}

	public final void setItemTaskFactory(ItemTaskFactory itemTaskFactory) {
		this.itemTaskFactory = itemTaskFactory;
	}
	
	public final HPRecoveryFactory getHPRecoveryFactory() {
		return hPRecoveryFactory;
	}

	public final void setHPRecoveryFactory(HPRecoveryFactory hPRecoveryFactory) {
		this.hPRecoveryFactory = hPRecoveryFactory;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("minFreeSlot", minFreeSlot);
		builder.append("rareItemSeuil", rareItemSeuil);
		builder.append("coinReserve", coinReserve);
		builder.append("minFreeInventorySpace", minFreeInventorySpace);
		return builder.toString();
	}
}
