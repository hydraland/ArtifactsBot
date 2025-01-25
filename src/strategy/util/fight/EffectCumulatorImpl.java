package strategy.util.fight;

import java.util.Arrays;

import hydra.GameConstants;

public final class EffectCumulatorImpl implements EffectCumulator {
	private static final int VALUE_INDEX = 0;
	private static final int QUANTITY_INDEX = 1;
	private int resWater;
	private int resEarth;
	private int resFire;
	private int resAir;
	private int dmgWater;
	private int dmgEarth;
	private int dmgFire;
	private int dmgAir;
	private int attackWater;
	private int attackEarth;
	private int attackFire;
	private int attackAir;
	private int hp;
	private final int[][] restore;
	private int boostHp;
	private int boostDmgAir;
	private int boostDmgWater;
	private int boostDmgEarth;
	private int boostDmgFire;
	private int boostResWater;
	private int boostResFire;
	private int boostResAir;
	private int boostResEarth;
	private int quantity;

	public EffectCumulatorImpl() {
		restore = new int[2][GameConstants.MAX_RESTORE_EQUIPED_ITEMS];
	}

	private void addRestoreEffectValues(int value, int quantity) {
		for (int i = 0; i < restore[VALUE_INDEX].length; i++) {
			if (restore[QUANTITY_INDEX][i] == 0) {
				restore[VALUE_INDEX][i] = value;
				restore[QUANTITY_INDEX][i] = quantity;
				break;
			}
		}
	}

	@Override
	public int getRestoreEffectValue(int quantity) {
		int value = 0;
		for (int i = 0; i < restore[VALUE_INDEX].length; i++) {
			if (restore[QUANTITY_INDEX][i] >= quantity) {
				value += restore[VALUE_INDEX][i];
			}
		}
		return value;
	}

	@Override
	public void reset() {
		resWater = 0;
		resEarth = 0;
		resFire = 0;
		resAir = 0;
		dmgWater = 0;
		dmgEarth = 0;
		dmgFire = 0;
		dmgAir = 0;
		attackWater = 0;
		attackEarth = 0;
		attackFire = 0;
		attackAir = 0;
		hp = 0;
		boostHp = 0;
		boostDmgAir = 0;
		boostDmgWater = 0;
		boostDmgEarth = 0;
		boostDmgFire = 0;
		boostResWater = 0;
		boostResFire = 0;
		boostResAir = 0;
		boostResEarth = 0;
		Arrays.fill(restore[VALUE_INDEX], 0);
		Arrays.fill(restore[QUANTITY_INDEX], 0);
	}

	@Override
	public boolean isRestore() {
		return restore[QUANTITY_INDEX][0] > 0;
	}

	@Override
	public void accumulate(ItemEffects itemEffects, int quantity) {
		this.quantity = quantity;
		itemEffects.addIn(this);
	}

	@Override
	public void addResWater(int value) {
		resWater += value;
	}

	@Override
	public void addResEarth(int value) {
		resEarth += value;
	}

	@Override
	public void addResFire(int value) {
		resFire += value;
	}

	@Override
	public void addResAir(int value) {
		resAir += value;
	}

	@Override
	public void addDmgWater(int value) {
		dmgWater += value;
	}

	@Override
	public void addDmgEarth(int value) {
		dmgEarth += value;
	}

	@Override
	public void addDmgFire(int value) {
		dmgFire += value;
	}

	@Override
	public void addDmgAir(int value) {
		dmgAir += value;
	}

	@Override
	public void addAttackWater(int value) {
		attackWater += value;
	}

	@Override
	public void addAttackEarth(int value) {
		attackEarth += value;
	}

	@Override
	public void addAttackFire(int value) {
		attackFire += value;
	}

	@Override
	public void addAttackAir(int value) {
		attackAir += value;
	}

	@Override
	public void addHp(int value) {
		hp += value;
	}

	@Override
	public void addRestore(int value) {
		addRestoreEffectValues(value, quantity);
	}

	@Override
	public void addBoostHp(int value) {
		boostHp += value;
	}

	@Override
	public void addBoostDmgAir(int value) {
		boostDmgAir += value;
	}

	@Override
	public void addBoostDmgWater(int value) {
		boostDmgWater += value;
	}

	@Override
	public void addBoostDmgEarth(int value) {
		boostDmgEarth += value;
	}

	@Override
	public void addBoostDmgFire(int value) {
		boostDmgFire += value;
	}

	@Override
	public void addBoostResWater(int value) {
		boostResWater += value;
	}

	@Override
	public void addBoostResFire(int value) {
		boostResFire += value;
	}

	@Override
	public void addBoostResAir(int value) {
		boostResAir += value;
	}

	@Override
	public void addBoostResEarth(int value) {
		boostResEarth += value;
	}

	@Override
	public final int getResWater() {
		return resWater;
	}

	@Override
	public final int getResEarth() {
		return resEarth;
	}

	@Override
	public final int getResFire() {
		return resFire;
	}

	@Override
	public final int getResAir() {
		return resAir;
	}

	@Override
	public final int getDmgWater() {
		return dmgWater;
	}

	@Override
	public final int getDmgEarth() {
		return dmgEarth;
	}

	@Override
	public final int getDmgFire() {
		return dmgFire;
	}

	@Override
	public final int getDmgAir() {
		return dmgAir;
	}

	@Override
	public final int getAttackWater() {
		return attackWater;
	}

	@Override
	public final int getAttackEarth() {
		return attackEarth;
	}

	@Override
	public final int getAttackFire() {
		return attackFire;
	}

	@Override
	public final int getAttackAir() {
		return attackAir;
	}

	@Override
	public final int getHp() {
		return hp;
	}

	@Override
	public final int getBoostHp() {
		return boostHp;
	}

	@Override
	public final int getBoostDmgAir() {
		return boostDmgAir;
	}

	@Override
	public final int getBoostDmgWater() {
		return boostDmgWater;
	}

	@Override
	public final int getBoostDmgEarth() {
		return boostDmgEarth;
	}

	@Override
	public final int getBoostDmgFire() {
		return boostDmgFire;
	}

	@Override
	public final int getBoostResWater() {
		return boostResWater;
	}

	@Override
	public final int getBoostResFire() {
		return boostResFire;
	}

	@Override
	public final int getBoostResAir() {
		return boostResAir;
	}

	@Override
	public final int getBoostResEarth() {
		return boostResEarth;
	}
}
