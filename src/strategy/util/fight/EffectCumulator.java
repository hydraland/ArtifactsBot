package strategy.util.fight;

public interface EffectCumulator {

	int getRestoreEffectValue(int quantity);

	void reset();

	boolean isRestore();

	void accumulate(ItemEffects itemEffects, int quantity);

	void addResWater(int value);

	void addResEarth(int value);

	void addResFire(int value);

	void addResAir(int value);

	void addDmgWater(int value);

	void addDmgEarth(int value);

	void addDmgFire(int value);

	void addDmgAir(int value);

	void addAttackWater(int value);

	void addAttackEarth(int value);

	void addAttackFire(int value);

	void addAttackAir(int value);

	void addHp(int value);

	void addRestore(int value);

	void addBoostHp(int value);

	void addBoostDmgAir(int value);

	void addBoostDmgWater(int value);

	void addBoostDmgEarth(int value);

	void addBoostDmgFire(int value);

	void addBoostResWater(int value);

	void addBoostResFire(int value);

	void addBoostResAir(int value);

	void addBoostResEarth(int value);

	int getResWater();

	int getResEarth();

	int getResFire();

	int getResAir();

	int getDmgWater();

	int getDmgEarth();

	int getDmgFire();

	int getDmgAir();

	int getAttackWater();

	int getAttackEarth();

	int getAttackFire();

	int getAttackAir();

	int getHp();

	int getBoostHp();

	int getBoostDmgAir();

	int getBoostDmgWater();

	int getBoostDmgEarth();

	int getBoostDmgFire();

	int getBoostResWater();

	int getBoostResFire();

	int getBoostResAir();

	int getBoostResEarth();
}
