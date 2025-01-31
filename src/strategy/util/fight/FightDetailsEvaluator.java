package strategy.util.fight;

public interface FightDetailsEvaluator {
	boolean isUpper(FightDetails oldFightDetails, FightDetails newFightDetails);
	boolean isEquals(FightDetails oldFightDetails, FightDetails newFightDetails);
	void setExtraCondition(ConditionEvaluator condition);
}
