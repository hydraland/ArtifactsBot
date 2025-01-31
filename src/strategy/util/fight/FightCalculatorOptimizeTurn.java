package strategy.util.fight;

import hydra.GameConstants;
import hydra.model.BotMonster;

public final class FightCalculatorOptimizeTurn implements FightCalculator {

	@Override
	public FightDetails calculateFightResult(BotMonster monster, int characterHp, int maxCharacterTurn,
			EffectCumulator effectsCumulator) {
		int characterDmg = calculCharacterDamage(monster, effectsCumulator);
		int characterTurn = calculTurns(monster.getHp(), characterDmg);
		if (characterTurn >= GameConstants.MAX_FIGHT_TURN) {
			// l'hypothèse c'est que l'on ne doit pas se retrouver dans ce cas, d'ou hp
			// character à -1
			return DEFAULT_FIGHT_DETAILS;
		}
		if (characterTurn > maxCharacterTurn) {
			// IL y a mieux donc on envoi un résultat par défaut
			return new FightDetails(false, characterTurn, characterTurn, Integer.MAX_VALUE, 0, 0, 0);
		}

		MonsterCalculStruct monsterResult = calculMonsterTurns(characterHp, monster, characterTurn, characterDmg,
				effectsCumulator);
		// Le calcul fait que l'on privilégie la non utilisation de potion quand cela
		// est possible
		int nbTurn = Math.min(characterTurn, monsterResult.monsterTurn());
		return new FightDetails(monsterResult.monsterTurn() >= characterTurn, nbTurn, characterTurn,
				monsterResult.characterLossHP(), monsterResult.restoreTurn(), characterDmg, monsterResult.monsterDmg());
	}

	private static final record MonsterCalculStruct(int monsterTurn, int restoreTurn, int characterLossHP,
			int monsterDmg) {
	}

	private int calculTurns(int hp, int dmg) {
		return dmg == 0 ? GameConstants.MAX_FIGHT_TURN : hp / dmg + (hp % dmg == 0 ? 0 : 1);
	}

	private MonsterCalculStruct calculMonsterTurns(int characterHp, BotMonster monster, int maxCharacterTurn,
			int characterDmg, EffectCumulator effectsCumulator) {
		int monsterDmg = calculMonsterDamage(monster, effectsCumulator);
		int characterMaxHp = characterHp + effectsCumulator.getHp();
		int characterMaxHpWithBoost = characterMaxHp + effectsCumulator.getBoostHp();
		int halfCharacterMaxHpWithBoost = characterMaxHpWithBoost / 2;
		if (!effectsCumulator.isRestore()) {
			int monsterTurn = calculTurns(characterMaxHpWithBoost, calculMonsterDamage(monster, effectsCumulator));

			int monsterTotalDmg = monsterDmg * (monsterTurn > maxCharacterTurn ? (maxCharacterTurn - 1) : monsterTurn);
			return new MonsterCalculStruct(monsterTurn, 0,
					Math.max(0, monsterTotalDmg - (characterMaxHpWithBoost - characterMaxHp)), monsterDmg);
		}
		int halfMonsterTurn = calculTurns(halfCharacterMaxHpWithBoost, calculMonsterDamage(monster, effectsCumulator));
		if (halfMonsterTurn >= maxCharacterTurn) {
			return new MonsterCalculStruct(halfMonsterTurn * 2, 0,
					Math.max(0, (maxCharacterTurn - 1) * monsterDmg - (characterMaxHpWithBoost - characterMaxHp)),
					monsterDmg);
		}
		int monsterTurn = halfMonsterTurn;
		int characterHP = characterMaxHpWithBoost - halfMonsterTurn * monsterDmg;
		int monsterHP = monster.getHp() - halfMonsterTurn * characterDmg;
		int restoreTurn = 1;
		while (characterHP >= 0 && monsterHP >= 0) {
			if (characterHP < halfCharacterMaxHpWithBoost) {
				int restoreValue = effectsCumulator.getRestoreEffectValue(restoreTurn);
				if (restoreValue > 0) {
					restoreTurn++;
				}
				characterHP += restoreValue;
			}
			monsterTurn++;
			monsterHP -= characterDmg;
			if (monsterHP > 0) {
				characterHP -= monsterDmg;
			}
		}
		return new MonsterCalculStruct(monsterTurn, restoreTurn - 1, Math.max(0, (characterMaxHp - characterHP)),
				monsterDmg);
	}

	private int calculMonsterDamage(BotMonster monster, EffectCumulator effectsCumulator) {
		int monsterEartDmg = (int) Math.rint(monster.getAttackEarth()
				* (1 - (effectsCumulator.getResEarth() + effectsCumulator.getBoostResEarth()) / 100d));
		int monsterAirDmg = (int) Math.rint(monster.getAttackAir()
				* (1 - (effectsCumulator.getResAir() + effectsCumulator.getBoostResAir()) / 100d));
		int monsterWaterDmg = (int) Math.rint(monster.getAttackWater()
				* (1 - (effectsCumulator.getResWater() + effectsCumulator.getBoostResWater()) / 100d));
		int monsterFireDmg = (int) Math.rint(monster.getAttackFire()
				* (1 - (effectsCumulator.getResFire() + effectsCumulator.getBoostResFire()) / 100d));
		return monsterEartDmg + monsterAirDmg + monsterWaterDmg + monsterFireDmg;
	}

	private int calculCharacterDamage(BotMonster monster, EffectCumulator effectsCumulator) {
		int characterEartDmg = calculEffectDamage(effectsCumulator.getAttackEarth(), effectsCumulator.getDmgEarth(),
				effectsCumulator.getBoostDmgEarth(), monster.getResEarth());
		int characterAirDmg = calculEffectDamage(effectsCumulator.getAttackAir(), effectsCumulator.getDmgAir(),
				effectsCumulator.getBoostDmgAir(), monster.getResAir());
		int characterWaterDmg = calculEffectDamage(effectsCumulator.getAttackWater(), effectsCumulator.getDmgWater(),
				effectsCumulator.getBoostDmgWater(), monster.getResWater());
		int characterFireDmg = calculEffectDamage(effectsCumulator.getAttackFire(), effectsCumulator.getDmgFire(),
				effectsCumulator.getBoostDmgFire(), monster.getResFire());
		return characterEartDmg + characterAirDmg + characterWaterDmg + characterFireDmg;
	}

	private int calculEffectDamage(float attackDmg, float dmgPercent, float dmgBoost, float monsterRes) {
		// On convertie le block en % de res
		return (int) Math.rint(Math.rint((attackDmg * (1d + (dmgPercent + dmgBoost) / 100d)))
				* (1d - (monsterRes + (monsterRes > 0 ? monsterRes * 0.1d : 0)) / 100d));
	}
}
