package strategy.util.fight;

import hydra.model.BotCharacter;
import hydra.model.BotMonster;
import strategy.util.BotItemInfo;
import strategy.util.OptimizeResult;
import util.Combinator;

public interface FightEvaluator {
	OptimizeResult evaluate(Combinator<BotItemInfo> combinator);

	OptimizeResult evaluate();

	void init(BotCharacter character, BotMonster monster, int characterHpWithoutEqt, boolean useUtilities);
}
