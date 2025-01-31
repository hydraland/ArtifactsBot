package strategy.util.fight;

public final class TurnDiffPowerFightDetailsEvaluator implements FightDetailsEvaluator {

	private ConditionEvaluator extraCondition;

	@Override
	public boolean isUpper(FightDetails oldFightDetails, FightDetails newFightDetails) {
		return newFightDetails.characterTurn() < oldFightDetails.characterTurn()
				|| (extraCondition.eval() && newFightDetails.characterTurn() == oldFightDetails.characterTurn()
						&& (uppper(newFightDetails, oldFightDetails)));
	}

	@Override
	public boolean isEquals(FightDetails oldFightDetails, FightDetails newFightDetails) {
		return (newFightDetails.characterTurn() == oldFightDetails.characterTurn())
				&& (!extraCondition.eval() || (equals(newFightDetails, oldFightDetails)));
	}

	@Override
	public void setExtraCondition(ConditionEvaluator condition) {
		this.extraCondition = condition;
	}

	public boolean uppper(FightDetails fightDetails, FightDetails fightDetails2) {
		return (fightDetails.characterDmg() - fightDetails.monsterDmg()) > (fightDetails2.characterDmg()
				- fightDetails2.monsterDmg());
	}

	public boolean equals(FightDetails fightDetails, FightDetails fightDetails2) {
		return (fightDetails.characterDmg() - fightDetails.monsterDmg()) == (fightDetails2.characterDmg()
				- fightDetails2.monsterDmg());
	}
}
