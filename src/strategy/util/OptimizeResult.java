package strategy.util;

import java.util.Arrays;

import strategy.util.fight.FightDetails;

public record OptimizeResult(FightDetails fightDetails, BotItemInfo[] bestEqt) {

	@Override
	public String toString() {
		return "OptimizeResult [eval=" + fightDetails + ", bestEqt=" + Arrays.toString(bestEqt) + "]";
	}
}
