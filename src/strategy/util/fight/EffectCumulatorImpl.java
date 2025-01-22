package strategy.util.fight;

import java.util.Arrays;

import hydra.GameConstants;
import hydra.model.BotEffect;

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
	public int getEffectValue(BotEffect effect) {
		switch (effect) {
		case ATTACK_AIR:
			return attackAir;
		case ATTACK_EARTH:
			return attackEarth;
		case ATTACK_FIRE:
			return attackFire;
		case ATTACK_WATER:
			return attackWater;
		case BOOST_DMG_AIR:
			return boostDmgAir;
		case BOOST_DMG_EARTH:
			return boostDmgEarth;
		case BOOST_DMG_FIRE:
			return boostDmgFire;
		case BOOST_DMG_WATER:
			return boostDmgWater;
		case BOOST_HP:
			return boostHp;
		case BOOST_RES_AIR:
			return boostResAir;
		case BOOST_RES_EARTH:
			return boostResEarth;
		case BOOST_RES_FIRE:
			return boostResFire;
		case BOOST_RES_WATER:
			return boostResWater;
		case DMG_AIR:
			return dmgAir;
		case DMG_EARTH:
			return dmgEarth;
		case DMG_FIRE:
			return dmgFire;
		case DMG_WATER:
			return dmgWater;
		case HP:
			return hp;
		case RES_AIR:
			return resAir;
		case RES_EARTH:
			return resEarth;
		case RES_FIRE:
			return resFire;
		case RES_WATER:
			return resWater;
		default:
			throw new IllegalArgumentException("Value  " + effect + " not authorize");
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
}
