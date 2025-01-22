package strategy.util.fight;

import hydra.model.BotEffect;

public interface EffectCumulator {
	int getEffectValue(BotEffect effect);

	int getRestoreEffectValue(int quantity);

	void reset();

	boolean isRestore();

	void accumulate(ItemEffects itemEffects, int quantity);

	public void addResWater(int value);

	public void addResEarth(int value);

	public void addResFire(int value);

	public void addResAir(int value);

	public void addDmgWater(int value);

	public void addDmgEarth(int value);

	public void addDmgFire(int value);

	public void addDmgAir(int value);

	public void addAttackWater(int value);

	public void addAttackEarth(int value);

	public void addAttackFire(int value);

	public void addAttackAir(int value);

	public void addHp(int value);

	public void addRestore(int value);

	public void addBoostHp(int value);

	public void addBoostDmgAir(int value);

	public void addBoostDmgWater(int value);

	public void addBoostDmgEarth(int value);

	public void addBoostDmgFire(int value);

	public void addBoostResWater(int value);

	public void addBoostResFire(int value);

	public void addBoostResAir(int value);

	public void addBoostResEarth(int value);

}
