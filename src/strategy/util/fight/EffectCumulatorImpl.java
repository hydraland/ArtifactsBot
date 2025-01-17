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

	public EffectCumulatorImpl() {
		restore = new int[2][GameConstants.MAX_RESTORE_EQUIPED_ITEMS];
	}

	@Override
	public void addEffectValue(BotEffect effect, int value) {
		switch (effect) {
		case ATTACK_AIR:
			attackAir += value;
			break;
		case ATTACK_EARTH:
			attackEarth += value;
			break;
		case ATTACK_FIRE:
			attackFire += value;
			break;
		case ATTACK_WATER:
			attackWater += value;
			break;
		case BOOST_DMG_AIR:
			boostDmgAir += value;
			break;
		case BOOST_DMG_EARTH:
			boostDmgEarth += value;
			break;
		case BOOST_DMG_FIRE:
			boostDmgFire += value;
			break;
		case BOOST_DMG_WATER:
			boostDmgWater += value;
			break;
		case BOOST_HP:
			boostHp += value;
			break;
		case BOOST_RES_AIR:
			boostResAir += value;
			break;
		case BOOST_RES_EARTH:
			boostResEarth += value;
			break;
		case BOOST_RES_FIRE:
			boostResFire += value;
			break;
		case BOOST_RES_WATER:
			boostResWater += value;
			break;
		case DMG_AIR:
			dmgAir += value;
			break;
		case DMG_EARTH:
			dmgEarth += value;
			break;
		case DMG_FIRE:
			dmgFire += value;
			break;
		case DMG_WATER:
			dmgWater += value;
			break;
		case HP:
			hp += value;
			break;
		case RES_AIR:
			resAir += value;
			break;
		case RES_EARTH:
			resEarth += value;
			break;
		case RES_FIRE:
			resFire += value;
			break;
		case RES_WATER:
			resWater += value;
			break;
		default:
			break;
		}
	}

	@Override
	public void addRestoreEffectValues(int value, int quantity) {
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
			if (restore[QUANTITY_INDEX][i] <= quantity) {
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
	public boolean isUpper(EffectCumulator effectCumulator) {
		if (effectCumulator instanceof EffectCumulatorImpl ec) {
			return resWater >= ec.resWater && resEarth >= ec.resEarth && resFire >= ec.resFire && resAir >= ec.resAir
					&& dmgWater >= ec.dmgWater && dmgEarth >= ec.dmgEarth && dmgFire >= ec.dmgFire
					&& dmgAir >= ec.dmgAir && attackWater >= ec.attackWater && attackEarth >= ec.attackEarth
					&& attackFire >= ec.attackFire && attackAir >= ec.attackAir && hp >= ec.hp && boostHp >= ec.boostHp
					&& boostDmgAir >= ec.boostDmgAir && boostDmgWater >= ec.boostDmgWater
					&& boostDmgEarth >= ec.boostDmgEarth && boostDmgFire >= ec.boostDmgFire
					&& boostResWater >= ec.boostResWater && boostResFire >= ec.boostResFire
					&& boostResAir >= ec.boostResAir && boostResEarth >= ec.boostResEarth
					&& restore[VALUE_INDEX][0] >= ec.restore[VALUE_INDEX][0];
		}
		return false;
	}

	@Override
	public boolean isRestore() {
		return restore[QUANTITY_INDEX][0] > 0;
	}
}
