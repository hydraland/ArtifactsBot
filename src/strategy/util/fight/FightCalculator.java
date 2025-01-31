package strategy.util.fight;

import hydra.GameConstants;
import hydra.model.BotMonster;

public interface FightCalculator {

	static final FightDetails DEFAULT_FIGHT_DETAILS = new FightDetails(false, GameConstants.MAX_FIGHT_TURN,
			GameConstants.MAX_FIGHT_TURN, Integer.MAX_VALUE, 0, 0, 0);

	FightDetails calculateFightResult(BotMonster monster, int characterHp, int maxCharacterTurn,
			EffectCumulator effectsCumulator);

	default FightDetails calculateFightResult(BotMonster monster, int characterHp, EffectCumulator effectsCumulator) {
		return calculateFightResult(monster, characterHp, GameConstants.MAX_FIGHT_TURN, effectsCumulator);
	}
}
