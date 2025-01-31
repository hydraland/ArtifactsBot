package strategy.util.fight;

public final class TurnRestoreLossHPFightDetailsEvaluator implements FightDetailsEvaluator {

	@Override
	public boolean isUpper(FightDetails oldFightDetails, FightDetails newFightDetails) {
		return oldFightDetails.win() ? newFightDetails.win() && (newFightDetails.characterTurn() < oldFightDetails
				.characterTurn()
				|| newFightDetails.characterTurn() == oldFightDetails.characterTurn()
						&& ((newFightDetails.restoreTurn() < oldFightDetails.restoreTurn())
								|| (newFightDetails.restoreTurn() == oldFightDetails.restoreTurn()
										&& newFightDetails.characterLossHP() < oldFightDetails.characterLossHP())))
				: (newFightDetails.win() || newFightDetails.characterTurn() < oldFightDetails.characterTurn());
	}

	@Override
	public boolean isEquals(FightDetails oldFightDetails, FightDetails newFightDetails) {
		return (newFightDetails.characterLossHP() == oldFightDetails.characterLossHP()
				&& newFightDetails.characterTurn() == oldFightDetails.characterTurn() && newFightDetails.win()
				&& oldFightDetails.win() && newFightDetails.restoreTurn() == oldFightDetails.restoreTurn());
	}

	@Override
	public void setExtraCondition(ConditionEvaluator condition) {
		// Not extra condition
	}
}
