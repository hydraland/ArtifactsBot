package strategy.util.fight;

import hydra.model.BotEffect;

public interface ItemEffects {
	void setEffectValue(BotEffect effect, int value);
	
	boolean isUpper(ItemEffects itemEffects);
	
	void addIn(EffectCumulator effectCumulator);
	
	public int getResWater();

	public int getResEarth();

	public int getResFire();

	public int getResAir();

	public int getDmgWater();

	public int getDmgEarth();

	public int getDmgFire();

	public int getDmgAir();

	public int getAttackWater();

	public int getAttackEarth();

	public int getAttackFire();

	public int getAttackAir();

	public int getHp();

	public int getRestore();

	public int getBoostHp();

	public int getBoostDmgAir();

	public int getBoostDmgWater();

	public int getBoostDmgEarth();

	public int getBoostDmgFire();

	public int getBoostResWater();

	public int getBoostResFire();

	public int getBoostResAir();

	public int getBoostResEarth();
}
