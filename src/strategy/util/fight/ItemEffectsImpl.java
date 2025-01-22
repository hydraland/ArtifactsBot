package strategy.util.fight;

import java.util.ArrayList;
import java.util.List;

import hydra.model.BotEffect;

public final class ItemEffectsImpl implements ItemEffects {
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
	private int restore;
	private int boostHp;
	private int boostDmgAir;
	private int boostDmgWater;
	private int boostDmgEarth;
	private int boostDmgFire;
	private int boostResWater;
	private int boostResFire;
	private int boostResAir;
	private int boostResEarth;
	private final List<EffectCumulatorAddFunction> addFunctions;

	public ItemEffectsImpl() {
		addFunctions = new ArrayList<EffectCumulatorAddFunction>();
	}

	@Override
	public void setEffectValue(BotEffect effect, int value) {
		switch (effect) {
		case ATTACK_AIR:
			attackAir = value;
			addFunctions.add(ec -> ec.addAttackAir(attackAir));
			break;
		case ATTACK_EARTH:
			attackEarth = value;
			addFunctions.add(ec -> ec.addAttackEarth(attackEarth));
			break;
		case ATTACK_FIRE:
			attackFire = value;
			addFunctions.add(ec -> ec.addAttackFire(attackFire));
			break;
		case ATTACK_WATER:
			attackWater = value;
			addFunctions.add(ec -> ec.addAttackWater(attackWater));
			break;
		case BOOST_DMG_AIR:
			boostDmgAir = value;
			addFunctions.add(ec -> ec.addBoostDmgAir(boostDmgAir));
			break;
		case BOOST_DMG_EARTH:
			boostDmgEarth = value;
			addFunctions.add(ec -> ec.addBoostDmgEarth(boostDmgEarth));
			break;
		case BOOST_DMG_FIRE:
			boostDmgFire += value;
			addFunctions.add(ec -> ec.addBoostDmgFire(boostDmgFire));
			break;
		case BOOST_DMG_WATER:
			boostDmgWater = value;
			addFunctions.add(ec -> ec.addBoostDmgWater(boostDmgWater));
			break;
		case BOOST_HP:
			boostHp = value;
			addFunctions.add(ec -> ec.addBoostHp(boostHp));
			break;
		case BOOST_RES_AIR:
			boostResAir = value;
			addFunctions.add(ec -> ec.addBoostResAir(boostResAir));
			break;
		case BOOST_RES_EARTH:
			boostResEarth = value;
			addFunctions.add(ec -> ec.addBoostResEarth(boostResEarth));
			break;
		case BOOST_RES_FIRE:
			boostResFire = value;
			addFunctions.add(ec -> ec.addBoostResFire(boostResFire));
			break;
		case BOOST_RES_WATER:
			boostResWater = value;
			addFunctions.add(ec -> ec.addBoostResWater(boostResWater));
			break;
		case DMG_AIR:
			dmgAir = value;
			addFunctions.add(ec -> ec.addDmgAir(dmgAir));
			break;
		case DMG_EARTH:
			dmgEarth = value;
			addFunctions.add(ec -> ec.addDmgEarth(dmgEarth));
			break;
		case DMG_FIRE:
			dmgFire = value;
			addFunctions.add(ec -> ec.addDmgFire(dmgFire));
			break;
		case DMG_WATER:
			dmgWater = value;
			addFunctions.add(ec -> ec.addDmgWater(dmgWater));
			break;
		case HP:
			hp = value;
			addFunctions.add(ec -> ec.addHp(hp));
			break;
		case RES_AIR:
			resAir = value;
			addFunctions.add(ec -> ec.addResAir(resAir));
			break;
		case RES_EARTH:
			resEarth = value;
			addFunctions.add(ec -> ec.addResEarth(resEarth));
			break;
		case RES_FIRE:
			resFire = value;
			addFunctions.add(ec -> ec.addResFire(resFire));
			break;
		case RES_WATER:
			resWater = value;
			addFunctions.add(ec -> ec.addResWater(resWater));
			break;
		case RESTORE:
			restore = value;
			addFunctions.add(ec -> ec.addRestore(restore));
			break;
		default:
			break;
		}
	}

	@Override
	public boolean isUpper(ItemEffects itemEffects) {
		return resWater >= itemEffects.getResWater() && resEarth >= itemEffects.getResEarth()
				&& resFire >= itemEffects.getResFire() && resAir >= itemEffects.getResAir()
				&& dmgWater >= itemEffects.getDmgWater() && dmgEarth >= itemEffects.getDmgEarth()
				&& dmgFire >= itemEffects.getDmgFire() && dmgAir >= itemEffects.getDmgAir()
				&& attackWater >= itemEffects.getAttackWater() && attackEarth >= itemEffects.getAttackEarth()
				&& attackFire >= itemEffects.getAttackFire() && attackAir >= itemEffects.getAttackAir()
				&& hp >= itemEffects.getHp() && boostHp >= itemEffects.getBoostHp()
				&& boostDmgAir >= itemEffects.getBoostDmgAir() && boostDmgWater >= itemEffects.getBoostDmgWater()
				&& boostDmgEarth >= itemEffects.getBoostDmgEarth() && boostDmgFire >= itemEffects.getBoostDmgFire()
				&& boostResWater >= itemEffects.getBoostResWater() && boostResFire >= itemEffects.getBoostResFire()
				&& boostResAir >= itemEffects.getBoostResAir() && boostResEarth >= itemEffects.getBoostResEarth()
				&& restore >= itemEffects.getRestore();
	}

	@Override
	public void addIn(EffectCumulator effectCumulator) {
		addFunctions.forEach(funct -> funct.add(effectCumulator));
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
	public final int getRestore() {
		return restore;
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
